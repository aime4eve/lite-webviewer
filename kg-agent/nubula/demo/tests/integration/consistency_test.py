#!/usr/bin/env python3
"""
Nebula Graph Consistency Test Script
Tests data consistency under concurrent operations
"""
import sys
import os
import time
import json
import threading
import random
from typing import List, Dict, Any

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import nebula_client

class ConsistencyTest:
    """Consistency Test Class"""
    
    def __init__(self):
        self.test_space = "consistency_test"
        self.results = {}
        self.errors = []
        self.lock = threading.Lock()
    
    def setup_test_space(self):
        """Setup test space"""
        print("Setting up test space...")
        
        # Create test space
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {self.test_space}")
        nebula_client.execute_query(f"CREATE SPACE IF NOT EXISTS {self.test_space} (partition_num=10, replica_factor=1)")
        
        # Wait for space creation
        time.sleep(10)
        
        # Init Schema
        nebula_client.init_schema(self.test_space)
        
        print("Test space setup completed")
    
    def cleanup_test_space(self):
        """Cleanup test space"""
        print("Cleaning up test space...")
        nebula_client.execute_query(f"DROP SPACE IF EXISTS {self.test_space}")
        print("Test space cleanup completed")
    
    def test_concurrent_entity_insertion(self, num_threads=5, entities_per_thread=20):
        """Test consistency of concurrent entity insertion"""
        print(f"\n=== Testing Concurrent Entity Insertion Consistency (Threads: {num_threads}, Entities/Thread: {entities_per_thread}) ===")
        
        # Record inserted entities
        inserted_entities = []
        
        # Define worker function
        def worker(thread_id):
            thread_entities = []
            
            for i in range(entities_per_thread):
                entity_id = f"thread_{thread_id}_entity_{i}"
                name = f"Thread{thread_id}Entity{i}"
                entity_type = f"Type{thread_id % 3}"
                description = f"Test entity {i} from thread {thread_id}"
                properties = f"{{thread_id: {thread_id}, index: {i}}}"
                
                # Insert entity
                try:
                    nebula_client.insert_entity(
                        entity_id, name, entity_type, description, properties, self.test_space
                    )
                    thread_entities.append({
                        "id": entity_id,
                        "name": name,
                        "type": entity_type,
                        "description": description,
                        "properties": properties
                    })
                except Exception as e:
                    with self.lock:
                        self.errors.append({
                            "operation": "insert_entity",
                            "thread_id": thread_id,
                            "entity_id": entity_id,
                            "error": str(e)
                        })
            
            with self.lock:
                inserted_entities.extend(thread_entities)
        
        # Create and start threads
        threads = []
        start_time = time.time()
        
        for i in range(num_threads):
            thread = threading.Thread(target=worker, args=(i,))
            threads.append(thread)
            thread.start()
        
        # Wait for all threads
        for thread in threads:
            thread.join()
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # Verify inserted entities
        print("Verifying inserted entities...")
        verified_entities = []
        missing_entities = []
        
        for entity in inserted_entities:
            try:
                result = nebula_client.execute_query(
                    f"MATCH (v:entity) WHERE v.entity_id == '{entity['id']}' RETURN v",
                    self.test_space
                )
                
                if result.is_succeeded() and len(result.rows()) > 0:
                    verified_entities.append(entity)
                else:
                    missing_entities.append(entity)
            except Exception as e:
                missing_entities.append(entity)
                with self.lock:
                    self.errors.append({
                        "operation": "verify_entity",
                        "entity_id": entity["id"],
                        "error": str(e)
                    })
        
        # Record results
        self.results["concurrent_entity_insertion"] = {
            "num_threads": num_threads,
            "entities_per_thread": entities_per_thread,
            "total_time": total_time,
            "expected_entities": num_threads * entities_per_thread,
            "inserted_entities": len(inserted_entities),
            "verified_entities": len(verified_entities),
            "missing_entities": len(missing_entities),
            "consistency_rate": len(verified_entities) / (num_threads * entities_per_thread) if num_threads * entities_per_thread > 0 else 0
        }
        
        print(f"Total Time: {total_time:.2f}s")
        print(f"Expected Entities: {num_threads * entities_per_thread}")
        print(f"Inserted Entities: {len(inserted_entities)}")
        print(f"Verified Entities: {len(verified_entities)}")
        print(f"Missing Entities: {len(missing_entities)}")
        print(f"Consistency Rate: {self.results['concurrent_entity_insertion']['consistency_rate']:.2%}")
    
    def test_concurrent_relationship_insertion(self, num_threads=5, relationships_per_thread=20):
        """Test consistency of concurrent relationship insertion"""
        print(f"\n=== Testing Concurrent Relationship Insertion Consistency (Threads: {num_threads}, Relationships/Thread: {relationships_per_thread}) ===")
        
        # Prepare entity data
        print("Preparing entity data...")
        for i in range(num_threads * relationships_per_thread):
            entity_id = f"rel_entity_{i}"
            nebula_client.insert_entity(
                entity_id, entity_id, "RelTestEntity", 
                f"RelTestEntity{i}", "{}", self.test_space
            )
        
        # Record inserted relationships
        inserted_relationships = []
        
        # Define worker function
        def worker(thread_id):
            thread_relationships = []
            
            for i in range(relationships_per_thread):
                # Randomly select source and target
                src_id = f"rel_entity_{random.randint(0, num_threads * relationships_per_thread - 1)}"
                dst_id = f"rel_entity_{random.randint(0, num_threads * relationships_per_thread - 1)}"
                
                # Avoid self-loop
                while src_id == dst_id:
                    dst_id = f"rel_entity_{random.randint(0, num_threads * relationships_per_thread - 1)}"
                
                relation_type = f"RelType{thread_id % 3}"
                weight = random.uniform(0.1, 1.0)
                description = f"Test relationship {i} from thread {thread_id}"
                
                # Insert relationship
                try:
                    nebula_client.insert_relationship(
                        src_id, dst_id, relation_type, weight, description, self.test_space
                    )
                    thread_relationships.append({
                        "src_id": src_id,
                        "dst_id": dst_id,
                        "type": relation_type,
                        "weight": weight,
                        "description": description
                    })
                except Exception as e:
                    with self.lock:
                        self.errors.append({
                            "operation": "insert_relationship",
                            "thread_id": thread_id,
                            "src_id": src_id,
                            "dst_id": dst_id,
                            "error": str(e)
                        })
            
            with self.lock:
                inserted_relationships.extend(thread_relationships)
        
        # Create and start threads
        threads = []
        start_time = time.time()
        
        for i in range(num_threads):
            thread = threading.Thread(target=worker, args=(i,))
            threads.append(thread)
            thread.start()
        
        # Wait for all threads
        for thread in threads:
            thread.join()
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # Verify inserted relationships
        print("Verifying inserted relationships...")
        verified_relationships = []
        missing_relationships = []
        
        for relationship in inserted_relationships:
            try:
                result = nebula_client.execute_query(
                    f"MATCH (v1:entity)-[e:relationship]->(v2:entity) "
                    f"WHERE v1.entity_id == '{relationship['src_id']}' AND v2.entity_id == '{relationship['dst_id']}' "
                    f"RETURN e",
                    self.test_space
                )
                
                if result.is_succeeded() and len(result.rows()) > 0:
                    verified_relationships.append(relationship)
                else:
                    missing_relationships.append(relationship)
            except Exception as e:
                missing_relationships.append(relationship)
                with self.lock:
                    self.errors.append({
                        "operation": "verify_relationship",
                        "src_id": relationship["src_id"],
                        "dst_id": relationship["dst_id"],
                        "error": str(e)
                    })
        
        # Record results
        self.results["concurrent_relationship_insertion"] = {
            "num_threads": num_threads,
            "relationships_per_thread": relationships_per_thread,
            "total_time": total_time,
            "expected_relationships": num_threads * relationships_per_thread,
            "inserted_relationships": len(inserted_relationships),
            "verified_relationships": len(verified_relationships),
            "missing_relationships": len(missing_relationships),
            "consistency_rate": len(verified_relationships) / (num_threads * relationships_per_thread) if num_threads * relationships_per_thread > 0 else 0
        }
        
        print(f"Total Time: {total_time:.2f}s")
        print(f"Expected Relationships: {num_threads * relationships_per_thread}")
        print(f"Inserted Relationships: {len(inserted_relationships)}")
        print(f"Verified Relationships: {len(verified_relationships)}")
        print(f"Missing Relationships: {len(missing_relationships)}")
        print(f"Consistency Rate: {self.results['concurrent_relationship_insertion']['consistency_rate']:.2%}")
    
    def test_read_write_consistency(self, num_threads=5, operations_per_thread=20):
        """Test read-write consistency"""
        print(f"\n=== Testing Read-Write Consistency (Threads: {num_threads}, Operations/Thread: {operations_per_thread}) ===")
        
        # Prepare initial entities
        print("Preparing initial entities...")
        initial_entities = []
        for i in range(50):
            entity_id = f"rw_entity_{i}"
            nebula_client.insert_entity(
                entity_id, entity_id, "RWTestEntity", 
                f"RWTestEntity{i}", "{}", self.test_space
            )
            initial_entities.append(entity_id)
        
        # Record results
        read_results = []
        write_results = []
        inconsistencies = []
        
        # Define worker function
        def worker(thread_id):
            thread_read_results = []
            thread_write_results = []
            thread_inconsistencies = []
            
            for i in range(operations_per_thread):
                operation = random.choice(["read", "write"])
                
                if operation == "read":
                    # Read operation
                    entity_id = random.choice(initial_entities)
                    try:
                        result = nebula_client.execute_query(
                            f"MATCH (v:entity) WHERE v.entity_id == '{entity_id}' RETURN v",
                            self.test_space
                        )
                        
                        if result.is_succeeded() and len(result.rows()) > 0:
                            thread_read_results.append({
                                "entity_id": entity_id,
                                "success": True
                            })
                        else:
                            thread_read_results.append({
                                "entity_id": entity_id,
                                "success": False
                            })
                    except Exception as e:
                        thread_read_results.append({
                            "entity_id": entity_id,
                            "success": False,
                            "error": str(e)
                        })
                
                elif operation == "write":
                    # Write operation
                    entity_id = random.choice(initial_entities)
                    new_name = f"UpdatedName_Thread{thread_id}_Op{i}"
                    
                    try:
                        nebula_client.execute_query(
                            f"MATCH (v:entity) WHERE v.entity_id == '{entity_id}' "
                            f"SET v.name = '{new_name}'",
                            self.test_space
                        )
                        
                        # Verify immediately
                        verify_result = nebula_client.execute_query(
                            f"MATCH (v:entity) WHERE v.entity_id == '{entity_id}' RETURN v.name",
                            self.test_space
                        )
                        
                        if verify_result.is_succeeded() and len(verify_result.rows()) > 0:
                            actual_name = verify_result.rows()[0][0].cast()
                            if actual_name == new_name:
                                thread_write_results.append({
                                    "entity_id": entity_id,
                                    "success": True,
                                    "consistent": True
                                })
                            else:
                                thread_write_results.append({
                                    "entity_id": entity_id,
                                    "success": True,
                                    "consistent": False,
                                    "expected": new_name,
                                    "actual": actual_name
                                })
                                thread_inconsistencies.append({
                                    "entity_id": entity_id,
                                    "operation": "write_then_read",
                                    "expected": new_name,
                                    "actual": actual_name
                                })
                        else:
                            thread_write_results.append({
                                "entity_id": entity_id,
                                "success": False,
                                "consistent": False
                            })
                            thread_inconsistencies.append({
                                "entity_id": entity_id,
                                "operation": "write_then_read",
                                "error": "Verification failed"
                            })
                    except Exception as e:
                        thread_write_results.append({
                            "entity_id": entity_id,
                            "success": False,
                            "error": str(e)
                        })
                        with self.lock:
                            self.errors.append({
                                "operation": "write",
                                "thread_id": thread_id,
                                "entity_id": entity_id,
                                "error": str(e)
                            })
            
            with self.lock:
                read_results.extend(thread_read_results)
                write_results.extend(thread_write_results)
                inconsistencies.extend(thread_inconsistencies)
        
        # Create and start threads
        threads = []
        start_time = time.time()
        
        for i in range(num_threads):
            thread = threading.Thread(target=worker, args=(i,))
            threads.append(thread)
            thread.start()
        
        # Wait for all threads
        for thread in threads:
            thread.join()
        
        end_time = time.time()
        total_time = end_time - start_time
        
        # Calculate stats
        successful_reads = sum(1 for r in read_results if r["success"])
        successful_writes = sum(1 for w in write_results if w["success"])
        consistent_writes = sum(1 for w in write_results if w.get("consistent", False))
        
        # Record results
        self.results["read_write_consistency"] = {
            "num_threads": num_threads,
            "operations_per_thread": operations_per_thread,
            "total_time": total_time,
            "total_operations": len(read_results) + len(write_results),
            "read_operations": len(read_results),
            "write_operations": len(write_results),
            "successful_reads": successful_reads,
            "successful_writes": successful_writes,
            "consistent_writes": consistent_writes,
            "read_success_rate": successful_reads / len(read_results) if read_results else 0,
            "write_success_rate": successful_writes / len(write_results) if write_results else 0,
            "write_consistency_rate": consistent_writes / len(write_results) if write_results else 0,
            "inconsistencies": len(inconsistencies)
        }
        
        print(f"Total Time: {total_time:.2f}s")
        print(f"Total Operations: {len(read_results) + len(write_results)}")
        print(f"Read Operations: {len(read_results)}, Success Rate: {self.results['read_write_consistency']['read_success_rate']:.2%}")
        print(f"Write Operations: {len(write_results)}, Success Rate: {self.results['read_write_consistency']['write_success_rate']:.2%}")
        print(f"Write Consistency Rate: {self.results['read_write_consistency']['write_consistency_rate']:.2%}")
        print(f"Inconsistencies: {len(inconsistencies)}")
    
    def save_results(self, filename="consistency_results.json"):
        """Save results"""
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump({
                "results": self.results,
                "errors": self.errors
            }, f, ensure_ascii=False, indent=2)
        
        print(f"\nTest results saved to {filename}")
    
    def print_summary(self):
        """Print summary"""
        print("\n=== Data Consistency Test Summary ===")
        
        if "concurrent_entity_insertion" in self.results:
            result = self.results["concurrent_entity_insertion"]
            print(f"\nConcurrent Entity Insertion Consistency: {result['consistency_rate']:.2%}")
        
        if "concurrent_relationship_insertion" in self.results:
            result = self.results["concurrent_relationship_insertion"]
            print(f"Concurrent Relationship Insertion Consistency: {result['consistency_rate']:.2%}")
        
        if "read_write_consistency" in self.results:
            result = self.results["read_write_consistency"]
            print(f"Read-Write Consistency: {result['write_consistency_rate']:.2%}")
        
        if self.errors:
            print(f"\nTotal Errors: {len(self.errors)}")

def main():
    """Main function"""
    print("=== Nebula Graph Data Consistency Test ===\n")
    
    consistency_test = ConsistencyTest()
    consistency_test.setup_test_space()
    
    try:
        consistency_test.test_concurrent_entity_insertion(num_threads=3, entities_per_thread=10)
        consistency_test.test_concurrent_relationship_insertion(num_threads=3, relationships_per_thread=10)
        consistency_test.test_read_write_consistency(num_threads=3, operations_per_thread=10)
        consistency_test.save_results("consistency_results.json")
        consistency_test.print_summary()
        print("\nData Consistency Test Completed!")
    
    finally:
        consistency_test.cleanup_test_space()

if __name__ == "__main__":
    main()