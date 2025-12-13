#!/usr/bin/env python
"""
Development runner for kg-agent.

Starts the Flask development server.

NOTE: This script must be run within a Python virtual environment.
To activate the virtual environment:

    # Linux/macOS
    source venv/bin/activate

    # Windows
    .\\venv\\Scripts\\activate

If you haven't created one yet:
    python -m venv venv
    pip install -r requirements.txt
"""
import os
import sys
import subprocess
import signal
import time

def is_venv():
    """Check if running in a virtual environment."""
    return (hasattr(sys, 'real_prefix') or
            (hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix))

def check_dependencies():
    """Check if required dependencies are installed."""
    # Core dependencies from requirements.txt
    required = [
        "flask", 
        "pydantic_settings", 
        "structlog", 
        "flask_cors",
        "celery",
        "redis",
        "pydantic",
        "elasticsearch",
        "pymilvus",
        "nebula3",  # nebula3-python
        "httpx",
        "dotenv",   # python-dotenv
        "flasgger"
    ]
    missing = []

    for dep in required:
        try:
            if dep == "nebula3":
                import nebula3
            elif dep == "dotenv":
                import dotenv
            else:
                __import__(dep)
        except ImportError:
            missing.append(dep)

    if missing:
        print(f"Missing dependencies: {', '.join(missing)}")
        print("Please ensure you have installed all dependencies:")
        print("pip install -r requirements.txt")
        return False

    return True

def start_server():
    """Start the Flask development server."""
    from src.backend.main import app, settings

    print(f"Starting kg-agent {settings.app_version}")
    print(f"Environment: {settings.environment}")
    print(f"Debug mode: {settings.debug}")
    print(f"Server: http://{settings.host}:{settings.port}")
    print(f"API prefix: {settings.api_prefix}")
    print("\nEndpoints:")
    print(f"  Health:    http://{settings.host}:{settings.port}/health")
    print(f"  API docs:  http://{settings.host}:{settings.port}/apidocs")
    print(f"  API base:  http://{settings.host}:{settings.port}{settings.api_prefix}")
    print("\nPress Ctrl+C to stop the server")

    # Start the Flask app
    app.run(
        host=settings.host,
        port=settings.port,
        debug=settings.debug,
        use_reloader=True
    )

def main():
    """Main entry point."""
    print("kg-agent Development Runner")
    print("=" * 40)

    # Check for virtual environment
    if not is_venv():
        print("\nERROR: This script must be run within a Python virtual environment.")
        print("-" * 40)
        print("Please activate your virtual environment and try again.")
        print("\nUsage:")
        print("  # Create venv (if not exists)")
        print("  python -m venv venv")
        print("\n  # Activate venv")
        print("  source venv/bin/activate  # Linux/macOS")
        print("  .\\venv\\Scripts\\activate  # Windows")
        print("\n  # Install dependencies")
        print("  pip install -r requirements.txt")
        print("-" * 40)
        sys.exit(1)

    # Check dependencies
    if not check_dependencies():
        sys.exit(1)

    # Start server
    try:
        start_server()
    except KeyboardInterrupt:
        print("\nServer stopped by user")
    except Exception as e:
        print(f"Error starting server: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()