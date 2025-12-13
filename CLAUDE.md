# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Nexus-Lite is a lightweight knowledge preview system that provides document scanning, indexing, and multi-format preview capabilities. It consists of three main components:

1. **Java Backend** (Spring Boot): Document scanning, preview generation, search, and REST API
2. **React Frontend**: Modern UI for browsing, searching, and previewing documents
3. **kg-agent** (Python Flask): Knowledge graph enhanced retrieval with GraphRAG capabilities

The system supports multiple file formats (PDF, DOCX, Markdown, CSV, XLSX, HTML, SVG) and provides full-text search via Elasticsearch with fallback to local search.

## Architecture and Key Technologies

### Backend (Java)
- **Framework**: Spring Boot 3.2.0 with Java 17
- **Database**: H2 (embedded file-based, `./data/nexus_db`)
- **ORM**: Spring Data JPA
- **Cache**: Caffeine
- **Search**: Elasticsearch 8.11.0 (optional, fallback to local search)
- **Document Processing**: PDFBox, Apache POI, Mammoth, Flexmark, OpenCSV, Tika
- **Build**: Maven (`backend/pom.xml`)

### Frontend (React)
- **Framework**: React 18.2.0 with Vite 5.0.8
- **UI Library**: Ant Design 5.12.0
- **State Management**: Zustand 4.4.7
- **Document Rendering**: pdfjs-dist, react-markdown, react-syntax-highlighter, mermaid, dompurify
- **Build**: npm (`frontend/package.json`)

### kg-agent (Python)
- **Framework**: Flask with Celery (Redis) for async tasks
- **Search Engines**: Elasticsearch (full-text), Milvus (vector), NebulaGraph (knowledge graph)
- **Dependency Management**: Poetry (`pyproject.toml`) and pip (`requirements.txt`)
- **API Documentation**: OpenAPI 3.0 with Swagger UI

### Key Architectural Patterns
- **Backend**: Domain-Driven Design (DDD) patterns in `backend/src/main/java/com/documentpreview/`
- **Frontend**: Component-based architecture with hooks
- **Integration**: RESTful APIs between components
- **Data Flow**: Documents scanned → indexed → searchable → previewable
- **Caching**: Multi-level caching for previews and indexes

## Common Development Commands

### One-Command Development Start
```bash
./start-dev.sh          # Starts frontend dev server, backend Spring Boot, and Elasticsearch
./start-dev.sh --build  # Builds project for production (frontend + backend)
```

### Backend (Java)
```bash
cd backend
mvn spring-boot:run              # Run Spring Boot (default port 8090)
mvn clean package -DskipTests    # Build JAR without tests
mvn test                         # Run tests
mvn spring-boot:run -Dserver.port=9090  # Run on custom port
```

### Frontend (React)
```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Start Vite dev server (port 5173)
npm run build        # Build for production
npm run lint         # Run ESLint
```

### kg-agent (Python)
```bash
cd kg-agent
poetry install                       # Install dependencies with Poetry
poetry run python -m src.backend.main  # Start Flask server (port 5000)
python run.py                        # Development runner

# Alternative with pip
pip install -r requirements.txt
python -m src.backend.main
```

### Full Production Build
```bash
./build.sh                     # Builds frontend, copies to backend static, packages JAR
./build.sh --run-tests         # Build with tests
```

## Configuration

### Backend Configuration
Primary config: `backend/src/main/resources/application.yml`
- Server port: 8090 (configurable via `SERVER_PORT` environment variable)
- Scan directories: `/root/lite-webviewer/docs` (configurable via `app.scan.root-dirs`)
- Database: H2 at `./data/nexus_db`
- Elasticsearch: `http://localhost:9200` (enable/disable with `app.search.use-es`)
- CORS: Enabled for all origins in development

### kg-agent Configuration
Environment variables via `.env` (copy from `.env.example`):
- Flask: `FLASK_PORT=5000`
- Redis: `REDIS_URL=redis://localhost:6379/0`
- Elasticsearch: `ELASTICSEARCH_HOSTS=http://localhost:9200`
- NebulaGraph: `NEBULA_GRAPH_HOST=localhost`, `NEBULA_GRAPH_PORT=9669`

## Important Paths and Ports

### Port Assignments
- Frontend dev server: 5173
- Backend Spring Boot: 8090 (default, configurable)
- Elasticsearch: 9200
- kg-agent Flask: 5000
- Redis (kg-agent): 6379
- NebulaGraph: 9669 (graphd), 9559 (metad), 9779/9780 (storaged)

### Key Directories
- `backend/src/main/java/com/documentpreview/` - Java source code
- `backend/src/main/resources/` - Configuration and static files
- `backend/data/` - H2 database files and indexes
- `frontend/src/` - React components and logic
- `kg-agent/src/backend/` - Python source code
- `data/files/` - User-uploaded documents (scanned directory)
- `logs/` - Application logs

### API Endpoints
- Backend API: `http://localhost:8090/api/` (see README.md for full list)
- kg-agent API: `http://localhost:5000/api/` (see kg-agent/README.md)
- Swagger UI (kg-agent): `http://localhost:5000/apidocs`
- Spring Boot Actuator: `http://localhost:8090/actuator/health`

## Integration Notes

### Document Scanning
- Backend automatically scans `app.scan.root-dirs` (default: `./docs`)
- Supports incremental updates and blacklisted directories (`.git`, `node_modules`)
- Generates TOON (Token-Oriented Object Notation) structures for documents

### Search Architecture
- Primary: Elasticsearch for full-text search
- Fallback: Local search when Elasticsearch is unavailable
- kg-agent adds hybrid search (full-text + vector + graph)

### Multi-Format Preview
Supported formats with appropriate rendering libraries:
- PDF: pdfjs-dist (frontend) + PDFBox (backend extraction)
- DOCX: Mammoth to HTML
- Markdown: Flexmark to HTML with syntax highlighting
- CSV/XLSX: Apache POI to HTML tables
- HTML/SVG: Sanitized display

### Knowledge Graph Integration
- kg-agent processes documents asynchronously via Celery tasks
- Extracts entities and relationships into NebulaGraph
- Provides advanced search through hybrid retrieval (GraphRAG)
- Integrated with main frontend via additional "Knowledge Graph" tab

## Git Conventions

### Commit Messages
Use semantic commit messages:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semi-colons)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks, dependency updates

### Branching Strategy
- `main`: Stable production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature development branches
- `bugfix/*`: Bug fix branches
- `release/*`: Release preparation branches

### Code Style
- **Backend**: Follow Google Java Style Guide
- **Frontend**: ESLint with Prettier formatting
- **kg-agent**: Black for formatting, flake8 for linting, mypy for type checking

## Development Workflow

1. **Start development environment**: `./start-dev.sh`
2. **Make changes** to backend (Java), frontend (React), or kg-agent (Python)
3. **Test changes** using appropriate test commands
4. **Build for production**: `./build.sh`
5. **Run production JAR**: `java -jar backend/target/nexus-lite-1.0.0-SNAPSHOT.jar`

## Troubleshooting

### Common Issues
- **Port conflicts**: Scripts attempt to find available ports automatically
- **Elasticsearch not running**: Backend falls back to local search; kg-agent requires it
- **Database issues**: H2 database file at `./data/nexus_db.mv.db`
- **kg-agent dependencies**: Ensure Redis, Elasticsearch, NebulaGraph are running

### Logs
- Backend: `backend.log` and `./logs/nexus-lite.log`
- Frontend: `frontend.log`
- kg-agent: Flask application logs

## References
- Main README: `README.md` (Chinese)
- kg-agent README: `kg-agent/README.md`
- API documentation in respective README files