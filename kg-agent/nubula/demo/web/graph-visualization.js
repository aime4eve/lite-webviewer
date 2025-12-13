// 图形可视化JavaScript代码
class GraphVisualization {
    constructor() {
        this.svg = null;
        this.simulation = null;
        this.nodes = [];
        this.edges = [];
        this.filteredNodes = [];
        this.filteredEdges = [];
        this.width = 0;
        this.height = 0;
        this.colorScale = d3.scaleOrdinal(d3.schemeCategory10);
        this.nodeTypeFilters = new Set();
        this.edgeTypeFilters = new Set();
        this.selectedNode = null;
        
        this.init();
        this.bindEvents();
        this.checkHealth();
        
        // Auto-load demo data
        d3.select("#keywords-input").property("value", "人工智能");
        this.queryGraph();
    }
    
    init() {
        const container = d3.select("#graph-container");
        this.width = container.node().getBoundingClientRect().width;
        this.height = container.node().getBoundingClientRect().height;
        
        this.svg = container.append("svg")
            .attr("width", this.width)
            .attr("height", this.height);
        
        // 添加缩放功能
        this.zoom = d3.zoom()
            .scaleExtent([0.1, 10])
            .on("zoom", (event) => {
                this.svg.select("g").attr("transform", event.transform);
            });
        
        this.svg.call(this.zoom);
        
        // 创建容器组
        this.svg.append("g").attr("class", "main-group");
        
        // 创建力导向图模拟
        this.simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(d => d.id).distance(100))
            .force("charge", d3.forceManyBody().strength(-300))
            .force("center", d3.forceCenter(this.width / 2, this.height / 2))
            .force("collision", d3.forceCollide().radius(30));
        
        // 创建工具提示
        this.tooltip = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);
        
        // 隐藏加载提示
        d3.select("#loading").style("display", "none");
    }
    
    bindEvents() {
        // 查询按钮
        d3.select("#query-btn").on("click", () => this.queryGraph());
        
        // 路径查找按钮
        d3.select("#path-btn").on("click", () => this.findPath());
        
        // 导出按钮
        d3.select("#export-btn").on("click", () => this.exportGraph());
        
        // 统计信息按钮
        d3.select("#statistics-btn").on("click", () => this.showStatistics());
        
        // 缩放控制按钮
        d3.select("#zoom-in-btn").on("click", () => this.zoomIn());
        d3.select("#zoom-out-btn").on("click", () => this.zoomOut());
        d3.select("#reset-zoom-btn").on("click", () => this.resetZoom());
        
        // 回车键触发查询
        d3.select("#keywords-input").on("keypress", (event) => {
            if (event.key === "Enter") {
                this.queryGraph();
            }
        });
    }
    
    async checkHealth() {
        try {
            const response = await fetch("/api/health");
            const data = await response.json();
            
            const statusIndicator = d3.select("#status-indicator");
            if (data.status === "ok") {
                statusIndicator.classed("status-ok", true).classed("status-error", false);
                statusIndicator.html("<i class=\"bi bi-check-circle\"></i> 连接正常");
            } else {
                statusIndicator.classed("status-ok", false).classed("status-error", true);
                statusIndicator.html("<i class=\"bi bi-exclamation-circle\"></i> 连接异常");
            }
        } catch (error) {
            const statusIndicator = d3.select("#status-indicator");
            statusIndicator.classed("status-ok", false).classed("status-error", true);
            statusIndicator.html("<i class=\"bi bi-exclamation-circle\"></i> 连接异常");
        }
    }
    
    async queryGraph() {
        const keywords = d3.select("#keywords-input").property("value").trim();
        const depth = parseInt(d3.select("#depth-input").property("value"));
        
        // Allow empty keywords to query all (limit applied in backend)
        
        d3.select("#loading").style("display", "block");
        
        try {
            const response = await fetch("/api/query", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    keywords: keywords ? keywords.split(",").map(k => k.trim()) : [],
                    depth: depth
                })
            });
            
            const data = await response.json();
            
            if (data.status === "success") {
                this.nodes = data.data.nodes;
                this.edges = data.data.edges;
                this.applyFilters();
                this.updateVisualization();
                this.updateFilterControls();
            } else {
                alert(`查询失败: ${data.message}`);
            }
        } catch (error) {
            alert(`查询失败: ${error.message}`);
        } finally {
            d3.select("#loading").style("display", "none");
        }
    }
    
    async findPath() {
        const srcNode = d3.select("#src-node-input").property("value").trim();
        const dstNode = d3.select("#dst-node-input").property("value").trim();
        
        if (!srcNode || !dstNode) {
            alert("请输入源节点和目标节点");
            return;
        }
        
        d3.select("#loading").style("display", "block");
        
        try {
            const response = await fetch(`/api/path?src=${encodeURIComponent(srcNode)}&dst=${encodeURIComponent(dstNode)}`);
            const data = await response.json();
            
            if (data.status === "success") {
                if (data.data.length === 0) {
                    alert("未找到路径");
                    return;
                }
                
                // 使用第一条路径的数据
                const path = data.data[0];
                this.nodes = path.nodes;
                this.edges = path.edges;
                this.applyFilters();
                this.updateVisualization();
                this.updateFilterControls();
            } else {
                alert(`查找路径失败: ${data.message}`);
            }
        } catch (error) {
            alert(`查找路径失败: ${error.message}`);
        } finally {
            d3.select("#loading").style("display", "none");
        }
    }
    
    async exportGraph() {
        d3.select("#loading").style("display", "block");
        
        try {
            const response = await fetch("/api/graph/export");
            const data = await response.json();
            
            if (data.status === "success") {
                this.nodes = data.data.nodes;
                this.edges = data.data.edges;
                this.applyFilters();
                this.updateVisualization();
                this.updateFilterControls();
                
                // 下载JSON文件
                const blob = new Blob([JSON.stringify(data.data, null, 2)], {type: "application/json"});
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "knowledge-graph.json";
                a.click();
                URL.revokeObjectURL(url);
            } else {
                alert(`导出图谱失败: ${data.message}`);
            }
        } catch (error) {
            alert(`导出图谱失败: ${error.message}`);
        } finally {
            d3.select("#loading").style("display", "none");
        }
    }
    
    async showStatistics() {
        d3.select("#loading").style("display", "block");
        
        try {
            const response = await fetch("/api/statistics");
            const data = await response.json();
            
            if (data.status === "success") {
                const stats = data.data;
                
                // 更新统计数据
                d3.select("#node-count").text(stats.node_count);
                d3.select("#edge-count").text(stats.edge_count);
                
                // 创建节点类型图表
                this.createTypeChart("node-type-chart", stats.node_types, "节点类型分布");
                
                // 创建边类型图表
                this.createTypeChart("edge-type-chart", stats.edge_types, "边类型分布");
                
                // 显示模态框
                const modal = new bootstrap.Modal(document.getElementById("statisticsModal"));
                modal.show();
            } else {
                alert(`获取统计信息失败: ${data.message}`);
            }
        } catch (error) {
            alert(`获取统计信息失败: ${error.message}`);
        } finally {
            d3.select("#loading").style("display", "none");
        }
    }
    
    createTypeChart(canvasId, typeData, title) {
        const ctx = document.getElementById(canvasId).getContext("2d");
        
        // 销毁现有图表
        if (this[`${canvasId}_chart`]) {
            this[`${canvasId}_chart`].destroy();
        }
        
        this[`${canvasId}_chart`] = new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: typeData.map(t => t.type),
                datasets: [{
                    data: typeData.map(t => t.count),
                    backgroundColor: typeData.map((_, i) => this.colorScale(i))
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: "bottom"
                    },
                    title: {
                        display: true,
                        text: title
                    }
                }
            }
        });
    }
    
    updateFilterControls() {
        // 获取所有节点类型
        const nodeTypes = [...new Set(this.nodes.map(n => n.type))];
        
        // 更新节点类型过滤器
        const nodeTypeFilters = d3.select("#node-type-filters");
        nodeTypeFilters.selectAll("*").remove();
        
        nodeTypes.forEach(type => {
            const div = nodeTypeFilters.append("div").attr("class", "form-check mb-1");
            div.append("input")
                .attr("class", "form-check-input")
                .attr("type", "checkbox")
                .attr("id", `node-type-${type}`)
                .attr("value", type)
                .property("checked", !this.nodeTypeFilters.has(type))
                .on("change", () => this.toggleNodeTypeFilter(type));
            
            div.append("label")
                .attr("class", "form-check-label")
                .attr("for", `node-type-${type}`)
                .text(type);
        });
        
        // 获取所有边类型
        const edgeTypes = [...new Set(this.edges.map(e => e.type))];
        
        // 更新边类型过滤器
        const edgeTypeFilters = d3.select("#edge-type-filters");
        edgeTypeFilters.selectAll("*").remove();
        
        edgeTypes.forEach(type => {
            const div = edgeTypeFilters.append("div").attr("class", "form-check mb-1");
            div.append("input")
                .attr("class", "form-check-input")
                .attr("type", "checkbox")
                .attr("id", `edge-type-${type}`)
                .attr("value", type)
                .property("checked", !this.edgeTypeFilters.has(type))
                .on("change", () => this.toggleEdgeTypeFilter(type));
            
            div.append("label")
                .attr("class", "form-check-label")
                .attr("for", `edge-type-${type}`)
                .text(type);
        });
    }
    
    toggleNodeTypeFilter(type) {
        if (this.nodeTypeFilters.has(type)) {
            this.nodeTypeFilters.delete(type);
        } else {
            this.nodeTypeFilters.add(type);
        }
        
        this.applyFilters();
        this.updateVisualization();
    }
    
    toggleEdgeTypeFilter(type) {
        if (this.edgeTypeFilters.has(type)) {
            this.edgeTypeFilters.delete(type);
        } else {
            this.edgeTypeFilters.add(type);
        }
        
        this.applyFilters();
        this.updateVisualization();
    }
    
    applyFilters() {
        // 应用节点类型过滤器
        this.filteredNodes = this.nodes.filter(node => !this.nodeTypeFilters.has(node.type));
        
        // 获取过滤后节点的ID集合
        const filteredNodeIds = new Set(this.filteredNodes.map(n => n.id));
        
        // 应用边类型过滤器，同时确保边的两端节点都在过滤后的节点集合中
        this.filteredEdges = this.edges.filter(edge => 
            !this.edgeTypeFilters.has(edge.type) &&
            filteredNodeIds.has(edge.source) &&
            filteredNodeIds.has(edge.target)
        );
    }
    
    updateVisualization() {
        const container = this.svg.select("g.main-group");
        
        // 清除现有元素
        container.selectAll("*").remove();
        
        // 创建边
        const link = container.append("g")
            .attr("class", "links")
            .selectAll("line")
            .data(this.filteredEdges)
            .enter().append("line")
            .attr("class", "link")
            .attr("stroke", d => this.colorScale(d.type))
            .attr("stroke-width", d => Math.sqrt(d.weight || 1) * 2)
            .attr("marker-end", "url(#arrowhead)");
        
        // 创建边标签
        const linkLabel = container.append("g")
            .attr("class", "link-labels")
            .selectAll("text")
            .data(this.filteredEdges)
            .enter().append("text")
            .attr("class", "link-label")
            .text(d => d.type || "");
        
        // 创建节点
        const node = container.append("g")
            .attr("class", "nodes")
            .selectAll("g")
            .data(this.filteredNodes)
            .enter().append("g")
            .attr("class", "node")
            .call(d3.drag()
                .on("start", (event, d) => this.dragstarted(event, d))
                .on("drag", (event, d) => this.dragged(event, d))
                .on("end", (event, d) => this.dragended(event, d)));
        
        // 添加节点圆圈
        node.append("circle")
            .attr("r", d => 10 + (d.importance === "high" ? 5 : 0))
            .attr("fill", d => this.colorScale(d.type))
            .attr("stroke", "#fff")
            .attr("stroke-width", 2)
            .on("mouseover", (event, d) => this.showTooltip(event, d))
            .on("mouseout", () => this.hideTooltip())
            .on("click", (event, d) => this.showNodeDetail(d));
        
        // 添加节点标签
        node.append("text")
            .attr("dy", -15)
            .text(d => d.name || d.id)
            .style("font-size", "12px")
            .style("font-weight", "bold");
        
        // 添加箭头标记
        this.svg.append("defs").selectAll("marker")
            .data(["arrowhead"])
            .enter().append("marker")
            .attr("id", "arrowhead")
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", 0)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr("fill", "#999");
        
        // 更新力导向图模拟
        this.simulation.nodes(this.filteredNodes);
        this.simulation.force("link").links(this.filteredEdges);
        this.simulation.alpha(1).restart();
        
        // 设置tick事件
        this.simulation.on("tick", () => {
            link
                .attr("x1", d => d.source.x)
                .attr("y1", d => d.source.y)
                .attr("x2", d => d.target.x)
                .attr("y2", d => d.target.y);
            
            linkLabel
                .attr("x", d => (d.source.x + d.target.x) / 2)
                .attr("y", d => (d.source.y + d.target.y) / 2);
            
            node.attr("transform", d => `translate(${d.x},${d.y})`);
        });
    }
    
    dragstarted(event, d) {
        if (!event.active) this.simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }
    
    dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }
    
    dragended(event, d) {
        if (!event.active) this.simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }
    
    showTooltip(event, d) {
        this.tooltip.transition()
            .duration(200)
            .style("opacity", .9);
        
        this.tooltip.html(`
            <strong>${d.name || d.id}</strong><br/>
            类型: ${d.type}<br/>
            ${d.description ? `描述: ${d.description}` : ''}
        `)
            .style("left", (event.pageX + 10) + "px")
            .style("top", (event.pageY - 28) + "px");
    }
    
    hideTooltip() {
        this.tooltip.transition()
            .duration(500)
            .style("opacity", 0);
    }
    
    async showNodeDetail(node) {
        this.selectedNode = node;
        
        // 显示节点详情面板
        const detailPanel = d3.select("#node-detail");
        detailPanel.style("display", "block");
        
        // 更新节点详情内容
        d3.select("#node-detail-title").text(node.name || node.id);
        
        let content = `
            <p><strong>ID:</strong> ${node.id}</p>
            <p><strong>类型:</strong> ${node.type}</p>
            ${node.description ? `<p><strong>描述:</strong> ${node.description}</p>` : ''}
        `;
        
        // 获取相关实体
        try {
            const response = await fetch(`/api/related/${node.id}`);
            const data = await response.json();
            
            if (data.status === "success" && data.data.length > 0) {
                content += "<h6>相关实体:</h6><ul>";
                data.data.forEach(entity => {
                    content += `<li>${entity.name} (${entity.type})</li>`;
                });
                content += "</ul>";
            }
        } catch (error) {
            console.error("获取相关实体失败:", error);
        }
        
        d3.select("#node-detail-content").html(content);
    }
    
    zoomIn() {
        this.svg.transition().call(
            this.zoom.scaleBy, 1.2
        );
    }
    
    zoomOut() {
        this.svg.transition().call(
            this.zoom.scaleBy, 0.8
        );
    }
    
    resetZoom() {
        this.svg.transition().call(
            this.zoom.transform,
            d3.zoomIdentity
        );
    }
}

// 初始化图形可视化
document.addEventListener("DOMContentLoaded", () => {
    new GraphVisualization();
});