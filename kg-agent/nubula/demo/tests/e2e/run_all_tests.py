#!/usr/bin/env python3
"""
Nebula Graph Test Suite Runner
Runs all test scripts and generates comprehensive report
"""
import sys
import os
import time
import json
import subprocess
import importlib.util
from typing import Dict, List, Any

# Add current directory to sys.path
sys.path.insert(0, os.path.dirname(__file__))

class TestSuiteRunner:
    """Test Suite Runner"""
    
    def __init__(self):
        self.test_results = {}
        # Scripts relative to tests/e2e/ directory (where this script is)
        self.test_scripts = [
            {
                "name": "Unit Tests (Direct)",
                "script": "../unit/test_nebula_direct.py",
                "description": "Basic connection and query tests (Direct)"
            },
            {
                "name": "Integration Tests (Suite)",
                "script": "test_suite.py",
                "description": "Integration functionality tests"
            },
            {
                "name": "Performance Tests",
                "script": "performance_test.py",
                "description": "Performance under different loads"
            },
            {
                "name": "Consistency Tests",
                "script": "../integration/consistency_test.py",
                "description": "Data consistency under concurrent operations"
            },
            {
                "name": "Load Tests",
                "script": "load_test.py",
                "description": "High load stability tests"
            }
        ]
        self.test_dir = os.path.dirname(__file__)
        self.project_root = os.path.abspath(os.path.join(self.test_dir, '../..'))
    
    def run_test_script(self, script_info: Dict[str, str]) -> Dict[str, Any]:
        """Run a single test script"""
        script_name = script_info["name"]
        script_rel_path = script_info["script"]
        
        print(f"\n{'='*60}")
        print(f"Running Test: {script_name}")
        print(f"Description: {script_info['description']}")
        print(f"Script: {script_rel_path}")
        print(f"{'='*60}")
        
        result = {
            "name": script_name,
            "script": script_rel_path,
            "description": script_info["description"],
            "start_time": time.time(),
            "success": False,
            "error": None,
            "output": "",
            "duration": 0
        }
        
        try:
            # Build full script path
            full_script_path = os.path.abspath(os.path.join(self.test_dir, script_rel_path))
            
            # Check if script exists
            if not os.path.exists(full_script_path):
                raise FileNotFoundError(f"Test script not found: {full_script_path}")
            
            # Run test script
            # Run from project root to ensure imports work if they assume root
            process = subprocess.run(
                [sys.executable, full_script_path],
                cwd=self.project_root,
                capture_output=True,
                text=True,
                timeout=300  # 5 min timeout
            )
            
            result["output"] = process.stdout
            result["error_output"] = process.stderr
            result["return_code"] = process.returncode
            result["success"] = process.returncode == 0
            
            if not result["success"]:
                result["error"] = f"Script failed with return code: {process.returncode}"
                if process.stderr:
                    result["error"] += f"\nError Output: {process.stderr}"
        
        except subprocess.TimeoutExpired:
            result["error"] = "Test script timed out"
        except Exception as e:
            result["error"] = f"Error running test script: {str(e)}"
        
        finally:
            result["end_time"] = time.time()
            result["duration"] = result["end_time"] - result["start_time"]
        
        # Print summary
        print(f"\nTest Result Summary:")
        print(f"  Success: {'Yes' if result['success'] else 'No'}")
        print(f"  Duration: {result['duration']:.2f}s")
        
        if not result["success"] and result["error"]:
            print(f"  Error: {result['error']}")
        
        return result
    
    def run_all_tests(self) -> Dict[str, Any]:
        """Run all test scripts"""
        print("Starting Nebula Graph Test Suite...")
        
        suite_result = {
            "start_time": time.time(),
            "test_results": {},
            "summary": {
                "total_tests": len(self.test_scripts),
                "successful_tests": 0,
                "failed_tests": 0,
                "total_duration": 0
            }
        }
        
        # Run each script
        for script_info in self.test_scripts:
            test_result = self.run_test_script(script_info)
            suite_result["test_results"][script_info["name"]] = test_result
            
            if test_result["success"]:
                suite_result["summary"]["successful_tests"] += 1
            else:
                suite_result["summary"]["failed_tests"] += 1
        
        # Calculate total duration
        suite_result["end_time"] = time.time()
        suite_result["summary"]["total_duration"] = suite_result["end_time"] - suite_result["start_time"]
        
        # Print summary
        self.print_suite_summary(suite_result)
        
        return suite_result
    
    def print_suite_summary(self, suite_result: Dict[str, Any]):
        """Print suite summary"""
        summary = suite_result["summary"]
        
        print(f"\n{'='*60}")
        print("Test Suite Execution Summary")
        print(f"{'='*60}")
        print(f"Total Tests: {summary['total_tests']}")
        print(f"Successful: {summary['successful_tests']}")
        print(f"Failed: {summary['failed_tests']}")
        print(f"Total Duration: {summary['total_duration']:.2f}s")
        if summary['total_tests'] > 0:
            print(f"Success Rate: {summary['successful_tests'] / summary['total_tests'] * 100:.1f}%")
        
        print("\nDetailed Results:")
        for test_name, result in suite_result["test_results"].items():
            status = "✓" if result["success"] else "✗"
            print(f"  {status} {test_name}: {result['duration']:.2f}s")
            
            if not result["success"] and result["error"]:
                print(f"    Error: {result['error']}")
    
    def generate_report(self, suite_result: Dict[str, Any], output_file="test_report.json"):
        """Generate test report"""
        report = {
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
            "suite_result": suite_result,
            "recommendations": []
        }
        
        # Analyze results
        for test_name, result in suite_result["test_results"].items():
            if not result["success"]:
                report["recommendations"].append(f"Check configuration and environment for {test_name}")
        
        # Save report
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        print(f"\nTest report saved to: {output_file}")
    
    def save_raw_results(self, suite_result: Dict[str, Any]):
        """Save raw results"""
        raw_results_dir = os.path.join(self.test_dir, "raw_results")
        os.makedirs(raw_results_dir, exist_ok=True)
        
        for test_name, result in suite_result["test_results"].items():
            # Save output
            output_file = os.path.join(raw_results_dir, f"{test_name}_output.txt")
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(result.get("output", ""))
            
            # Save error output
            if "error_output" in result and result["error_output"]:
                error_file = os.path.join(raw_results_dir, f"{test_name}_error.txt")
                with open(error_file, 'w', encoding='utf-8') as f:
                    f.write(result["error_output"])
        
        print(f"\nRaw results saved to: {raw_results_dir}")

def main():
    """Main function"""
    print("Nebula Graph Test Suite Runner")
    
    runner = TestSuiteRunner()
    suite_result = runner.run_all_tests()
    runner.generate_report(suite_result)
    runner.save_raw_results(suite_result)
    
    print("\nTest Suite Execution Completed!")

if __name__ == "__main__":
    main()