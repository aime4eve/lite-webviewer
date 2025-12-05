#!/usr/bin/env python
"""
Development runner for kg-agent.

Starts the Flask development server.
"""
import os
import sys
import subprocess
import signal
import time

def check_dependencies():
    """Check if required dependencies are installed."""
    required = ["flask", "pydantic-settings", "structlog", "flask_cors"]
    missing = []

    for dep in required:
        try:
            __import__(dep.replace("-", "_"))
        except ImportError:
            missing.append(dep)

    if missing:
        print(f"Missing dependencies: {', '.join(missing)}")
        print("Install with: pip install " + " ".join(missing))
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