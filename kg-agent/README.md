# kg-agent - Knowledge Graph Enhanced Retrieval Agent

A Python-based GraphRAG (Graph Retrieval-Augmented Generation) system designed to enhance document search with knowledge graph capabilities. This service integrates with the existing Nexus-Lite document preview system to provide advanced search, entity recognition, and relationship discovery.

## Architecture

```
[React Frontend] → [Spring Boot (8080)] → Basic search, document preview
                 → [kg-agent Flask (5000)] → Advanced search, knowledge graph
                                           → [NebulaGraph] + [Elasticsearch] + [Milvus]
```

## Features

- **Multi-modal Retrieval**: Full-text search (Elasticsearch), vector search (Milvus), and graph search (NebulaGraph)
- **Knowledge Graph**: Entity recognition, relationship extraction, and graph traversal
- **Document Processing**: Multi-format support (PDF, DOCX, Markdown, etc.) with semantic chunking
- **Async Processing**: Celery-based task queue for document processing pipelines
- **RESTful APIs**: OpenAPI 3.0 compliant API with Swagger UI

## Quick Start

### Prerequisites

- Python 3.11+
- Poetry (dependency management) or pip
- Local services: Redis, Elasticsearch, NebulaGraph (see [Local Services Setup](#local-services-setup))

### Development Setup

1. **Clone and navigate to project**:
   ```bash
   cd kg-agent
   ```

2. **Install dependencies**:
   ```bash
   poetry install
   ```

3. **Set up environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Ensure local services are running**:
   Make sure Redis, Elasticsearch, and NebulaGraph are installed and running locally. See [Local Services Setup](#local-services-setup) for installation instructions.

5. **Run the Flask application**:
   ```bash
   poetry run python -m src.backend.main
   ```

6. **Access services**:
   - Flask API: http://localhost:5000
   - API Docs: http://localhost:5000/apidocs
   - Health check: http://localhost:5000/health

## Local Services Setup

kg-agent depends on several services that need to be installed and running locally:

### Redis
- **Purpose**: Message broker for Celery and caching
- **Installation**:
  - **Windows**: Download from [Redis for Windows](https://github.com/microsoftarchive/redis/releases) or use WSL
  - **macOS**: `brew install redis`
  - **Linux**: `sudo apt-get install redis-server` (Ubuntu/Debian)
- **Start Redis**: `redis-server` (default port 6379)

### Elasticsearch
- **Purpose**: Full-text search engine
- **Installation**:
  - Download from [Elasticsearch Download](https://www.elastic.co/downloads/elasticsearch)
  - Extract and run `bin/elasticsearch` (or `bin\elasticsearch.bat` on Windows)
- **Configuration**: Default port 9200, no authentication by default
- **Note**: Requires Java 11+

### NebulaGraph
- **Purpose**: Knowledge graph storage and query
- **Installation**:
  - Download from [NebulaGraph Download](https://github.com/vesoft-inc/nebula/releases)
  - Follow platform-specific installation guides
- **Components**: Need to start `nebula-graphd`, `nebula-metad`, `nebula-storaged`
- **Default ports**: 9669 (graphd), 9559 (metad), 9779/9780 (storaged)

### Milvus (Optional)
- **Purpose**: Vector similarity search
- **Installation**:
  - **Milvus Lite** (Embedded): `pip install milvus` (includes embedded server)
  - **Standalone**: Download from [Milvus Release](https://github.com/milvus-io/milvus/releases)
  - Client library: `pip install pymilvus`
- **Note**: For development, Milvus Lite is recommended as it runs embedded in Python

### Verification
After installing services, verify they're running:
```bash
# Check Redis
redis-cli ping  # Should return "PONG"

# Check Elasticsearch
curl http://localhost:9200/  # Should return cluster info

# Check NebulaGraph
# Connect using nebula-console or check ports
```

Update `.env` file with correct host/port if different from defaults.

## Project Structure

```
kg-agent/
├── src/backend/                 # Source code
│   ├── app/                    # Application modules
│   │   ├── api/               # API blueprints (search, kg, admin)
│   │   ├── services/          # Business logic
│   │   ├── models/            # Pydantic models
│   │   ├── tasks/             # Celery task definitions
│   │   └── utils/             # Utility functions
│   ├── config.py              # Configuration (Pydantic Settings)
│   └── main.py               # Flask application entry
├── tests/                     # Test suites
├── docs/                      # Documentation
├── pyproject.toml            # Dependencies (Poetry)
├── run.py                    # Development runner script
├── requirements.txt          # Quick install dependencies
└── .env.example              # Environment variables template
```

## API Endpoints

### Health Checks
- `GET /health` - Service health status
- `GET /health/ready` - Readiness with dependency checks
- `GET /health/live` - Liveness probe

### Search API (`/api/search/*`)
- `POST /api/search/advanced` - Hybrid search (full-text + vector + graph)
- `GET /api/search/simple` - Simple keyword search
- `GET /api/search/semantic` - Semantic vector search

### Knowledge Graph API (`/api/kg/*`)
- `GET /api/kg/entities` - List entities with filtering
- `GET /api/kg/entities/{id}` - Get entity details
- `GET /api/kg/relationships` - Explore entity relationships
- `GET /api/kg/path/{from}/{to}` - Find paths between entities
- `POST /api/kg/query` - Execute custom graph queries

### Admin API (`/api/admin/*`)
- `POST /api/admin/index/rebuild` - Rebuild all indices
- `GET /api/admin/tasks` - List background tasks
- `GET /api/admin/metrics` - System metrics

## Development

### Code Quality

```bash
# Format code
poetry run black src/ tests/

# Lint
poetry run flake8 src/ tests/

# Type checking
poetry run mypy src/

# Run tests
poetry run pytest tests/ -v
```

### Adding New Dependencies

```bash
# Production dependency
poetry add package-name

# Development dependency
poetry add --group dev package-name
```

## Configuration

Configuration is managed through environment variables (see `.env.example`). Key configuration areas:

- **Storage**: Elasticsearch, NebulaGraph, Milvus, Redis connections
- **Models**: Embedding and NER model paths and devices
- **Processing**: Chunk size, overlap, file size limits
- **Security**: CORS origins, secret keys

## Integration with Nexus-Lite

kg-agent is designed to complement the existing Nexus-Lite system:

1. **Document Processing**: kg-agent processes documents through async pipelines
2. **Enhanced Search**: Provides advanced search capabilities beyond basic keyword matching
3. **Knowledge Discovery**: Extracts entities and relationships for graph exploration
4. **Unified Frontend**: Same React frontend with additional "Knowledge Graph" tab

## Monitoring

- **Application Logs**: Structured JSON logging via structlog
- **Metrics**: Prometheus metrics endpoint at `/metrics`
- **Task Monitoring**: Flower dashboard for Celery tasks (optional)

## License

[License Type - To be determined]

## Support

For issues and feature requests, please use the project issue tracker.