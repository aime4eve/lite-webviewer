#!/usr/bin/env python3
"""
Nebula Graph 性能测试脚本
测试系统在不同负载下的性能表现
"""
import sys
import os
import time
import json
import random
import statistics
from typing import List, Dict, Any

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import nebula_client

class PerformanceTest:
    """性能测试类"""
    
    def __init__(self):
        self.results = {}
        self.test_data = {
            "entities": [],
            "relationships": []
        }
    
    def generate_test_data(self, num_entities=1000, avg_relationships_per_entity=5):
        """生成测试数据"""
        print(f"生成测试数据: {num_entities}个实体, 平均每个实体{avg_relationships_per_entity}个关系...")
        
        # 生成实体
        entity_types = ["技术", "概念", "模型", "算法", "应用", "工具", "框架", "平台"]
        
        for i in range(num_entities):
            entity_id = f"entity_{i}"
            name = f"实体_{i}"
            entity_type = random.choice(entity_types)
            description = f"这是第{i}个测试实体，类型为{entity_type}"
            properties = f"{{index: {i}, type: '{entity_type}'}}"
            
            self.test_data["entities"].append({
                "id": entity_id,
                "name": name,
                "type": entity_type,
                "description": description,
                "properties": properties
            })
        
        # 生成关系
        relationship_types = ["包含", "基于", "应用", "实现", "依赖", "扩展", "优化", "替代"]
        
        for i in range(num_entities):
            # 每个实体平均有avg_relationships_per_entity个关系
            num_relationships = random.randint(1, avg_relationships_per_entity * 2)
            
            for _ in range(num_relationships):
                # 随机选择源和目标实体
                src_id = f"entity_{i}"
                dst_id = f"entity_{random.randint(0, num_entities-1)}"
                
                # 避免自环
                if src_id == dst_id:
                    continue
                
                relation_type = random.choice(relationship_types)
                weight = round(random.uniform(0.1, 1.0), 2)
                description = f"{src_id} {relation_type} {dst_id}"
                
                self.test_data["relationships"].append({
                    "src_id": src_id,
                    "dst_id": dst_id,
                    "type": relation_type,
                    "weight": weight,
                    "description": description
                })
        
        print(f"测试数据生成完成: {len(self.test_data['entities'])}个实体, {len(self.test_data['relationships'])}个关系")
    
    def test_insert_performance(self, batch_sizes=[10, 50, 100, 500]):
        """测试插入性能"""
        print("\n=== 测试插入性能 ===")
        
        self.results["insert"] = {}
        
        for batch_size in batch_sizes:
            print(f"\n测试批量大小: {batch_size}")
            
            # 创建测试空间
            test_space = f"perf_test_insert_{batch_size}"
            nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
            nebula_client.execute_query(f"CREATE SPACE IF NOT EXISTS {test_space} (partition_num=10, replica_factor=1, vid_type=FIXED_STRING(256))")
            
            # 等待空间创建完成
            time.sleep(10)
            
            # 初始化Schema
            nebula_client.init_schema(test_space)
            
            # 测试实体插入性能
            entity_times = []
            num_batches = min(10, len(self.test_data["entities"]) // batch_size)
            
            for i in range(num_batches):
                start_time = time.time()
                
                # 插入一批实体
                for j in range(batch_size):
                    idx = i * batch_size + j
                    if idx >= len(self.test_data["entities"]):
                        break
                    
                    entity = self.test_data["entities"][idx]
                    nebula_client.insert_entity(
                        entity["id"], entity["name"], entity["type"],
                        entity["description"], entity["properties"]
                    )
                
                end_time = time.time()
                batch_time = end_time - start_time
                entity_times.append(batch_time)
                
                print(f"  批次 {i+1}/{num_batches}: {batch_time:.2f}秒")
            
            # 计算统计信息
            avg_time = statistics.mean(entity_times)
            min_time = min(entity_times)
            max_time = max(entity_times)
            throughput = batch_size / avg_time
            
            self.results["insert"][batch_size] = {
                "avg_time": avg_time,
                "min_time": min_time,
                "max_time": max_time,
                "throughput": throughput
            }
            
            print(f"  平均时间: {avg_time:.2f}秒")
            print(f"  最小时间: {min_time:.2f}秒")
            print(f"  最大时间: {max_time:.2f}秒")
            print(f"  吞吐量: {throughput:.2f} 实体/秒")
            
            # 清理测试空间
            nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
    
    def test_query_performance(self, query_depths=[1, 2, 3, 4]):
        """测试查询性能"""
        print("\n=== 测试查询性能 ===")
        
        # 创建测试空间并插入数据
        test_space = "perf_test_query"
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
        nebula_client.execute_query(f"CREATE SPACE IF NOT EXISTS {test_space} (partition_num=10, replica_factor=1)")
        
        # 等待空间创建完成
        time.sleep(10)
        
        # 初始化Schema
        nebula_client.init_schema(test_space)
        
        # 插入测试数据
        print("插入测试数据...")
        for entity in self.test_data["entities"]:
            nebula_client.insert_entity(
                entity["id"], entity["name"], entity["type"],
                entity["description"], entity["properties"]
            )
        
        for relationship in self.test_data["relationships"]:
            nebula_client.insert_relationship(
                relationship["src_id"], relationship["dst_id"],
                relationship["type"], relationship["weight"],
                relationship["description"]
            )
        
        print("测试数据插入完成")
        
        self.results["query"] = {}
        
        for depth in query_depths:
            print(f"\n测试查询深度: {depth}")
            
            query_times = []
            num_queries = 10
            
            for i in range(num_queries):
                # 随机选择查询关键词
                keywords = []
                for _ in range(random.randint(1, 3)):
                    entity = random.choice(self.test_data["entities"])
                    keywords.append(entity["name"])
                
                start_time = time.time()
                
                # 执行查询
                result = nebula_client.query_entities(keywords, depth)
                
                end_time = time.time()
                query_time = end_time - start_time
                query_times.append(query_time)
                
                print(f"  查询 {i+1}/{num_queries}: {query_time:.2f}秒, 返回 {len(result['nodes'])} 节点, {len(result['edges'])} 边")
            
            # 计算统计信息
            avg_time = statistics.mean(query_times)
            min_time = min(query_times)
            max_time = max(query_times)
            
            self.results["query"][depth] = {
                "avg_time": avg_time,
                "min_time": min_time,
                "max_time": max_time
            }
            
            print(f"  平均时间: {avg_time:.2f}秒")
            print(f"  最小时间: {min_time:.2f}秒")
            print(f"  最大时间: {max_time:.2f}秒")
        
        # 清理测试空间
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
    
    def test_concurrent_performance(self, num_threads=5, operations_per_thread=20):
        """测试并发性能"""
        print("\n=== 测试并发性能 ===")
        
        import threading
        
        # 创建测试空间
        test_space = "perf_test_concurrent"
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
        nebula_client.execute_query(f"CREATE SPACE IF NOT EXISTS {test_space} (partition_num=10, replica_factor=1)")
        
        # 等待空间创建完成
        time.sleep(10)
        
        # 初始化Schema
        nebula_client.init_schema(test_space)
        
        # 定义线程函数
        def worker(thread_id, results):
            thread_times = []
            
            for i in range(operations_per_thread):
                operation = random.choice(["insert_entity", "insert_relationship", "query"])
                
                start_time = time.time()
                
                if operation == "insert_entity":
                    # 插入实体
                    entity_id = f"thread_{thread_id}_entity_{i}"
                    nebula_client.insert_entity(
                        entity_id, entity_id, "测试类型", 
                        f"线程{thread_id}的测试实体{i}", "{}"
                    )
                elif operation == "insert_relationship":
                    # 插入关系
                    src_id = f"entity_{random.randint(0, min(100, len(self.test_data['entities'])-1))}"
                    dst_id = f"entity_{random.randint(0, min(100, len(self.test_data['entities'])-1))}"
                    nebula_client.insert_relationship(
                        src_id, dst_id, "测试关系", 0.5, 
                        f"线程{thread_id}的测试关系{i}"
                    )
                else:
                    # 查询
                    keywords = [f"entity_{random.randint(0, min(100, len(self.test_data['entities'])-1))}"]
                    nebula_client.query_entities(keywords, 2)
                
                end_time = time.time()
                operation_time = end_time - start_time
                thread_times.append(operation_time)
            
            results[thread_id] = {
                "times": thread_times,
                "avg_time": statistics.mean(thread_times),
                "min_time": min(thread_times),
                "max_time": max(thread_times)
            }
        
        # 创建并启动线程
        threads = []
        thread_results = {}
        
        start_time = time.time()
        
        for i in range(num_threads):
            thread = threading.Thread(target=worker, args=(i, thread_results))
            threads.append(thread)
            thread.start()
        
        # 等待所有线程完成
        for thread in threads:
            thread.join()
        
        end_time = time.time()
        total_time = end_time - start_time
        total_operations = num_threads * operations_per_thread
        overall_throughput = total_operations / total_time
        
        # 计算统计信息
        all_times = []
        for thread_id, result in thread_results.items():
            all_times.extend(result["times"])
            print(f"  线程 {thread_id}: 平均时间 {result['avg_time']:.2f}秒")
        
        avg_time = statistics.mean(all_times)
        min_time = min(all_times)
        max_time = max(all_times)
        
        self.results["concurrent"] = {
            "num_threads": num_threads,
            "operations_per_thread": operations_per_thread,
            "total_time": total_time,
            "total_operations": total_operations,
            "overall_throughput": overall_throughput,
            "avg_time": avg_time,
            "min_time": min_time,
            "max_time": max_time
        }
        
        print(f"\n总时间: {total_time:.2f}秒")
        print(f"总操作数: {total_operations}")
        print(f"整体吞吐量: {overall_throughput:.2f} 操作/秒")
        print(f"平均操作时间: {avg_time:.2f}秒")
        print(f"最小操作时间: {min_time:.2f}秒")
        print(f"最大操作时间: {max_time:.2f}秒")
        
        # 清理测试空间
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {test_space}")
    
    def save_results(self, filename="performance_results.json"):
        """保存测试结果"""
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(self.results, f, ensure_ascii=False, indent=2)
        
        print(f"\n测试结果已保存到 {filename}")
    
    def print_summary(self):
        """打印测试结果摘要"""
        print("\n=== 性能测试摘要 ===")
        
        if "insert" in self.results:
            print("\n插入性能:")
            for batch_size, result in self.results["insert"].items():
                print(f"  批量大小 {batch_size}: {result['throughput']:.2f} 实体/秒")
        
        if "query" in self.results:
            print("\n查询性能:")
            for depth, result in self.results["query"].items():
                print(f"  查询深度 {depth}: {result['avg_time']:.2f}秒")
        
        if "concurrent" in self.results:
            print("\n并发性能:")
            result = self.results["concurrent"]
            print(f"  {result['num_threads']} 线程: {result['overall_throughput']:.2f} 操作/秒")

def main():
    """主函数"""
    print("=== Nebula Graph 性能测试 ===\n")
    
    # 创建性能测试实例
    perf_test = PerformanceTest()
    
    # 生成测试数据
    perf_test.generate_test_data(num_entities=500, avg_relationships_per_entity=3)
    
    # 运行插入性能测试
    perf_test.test_insert_performance(batch_sizes=[10, 50, 100])
    
    # 运行查询性能测试
    perf_test.test_query_performance(query_depths=[1, 2, 3])
    
    # 运行并发性能测试
    perf_test.test_concurrent_performance(num_threads=3, operations_per_thread=10)
    
    # 保存结果
    perf_test.save_results("performance_results.json")
    
    # 打印摘要
    perf_test.print_summary()
    
    print("\n性能测试完成！")

if __name__ == "__main__":
    main()