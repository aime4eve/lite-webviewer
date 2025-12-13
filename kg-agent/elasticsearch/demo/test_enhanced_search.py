from full_text_search import FullTextSearchService
import json

def test_search():
    service = FullTextSearchService()
    
    # Test 1: Wildcard search
    print("\n--- Testing Wildcard Search (*.txt) ---")
    results = service.search("*.txt", size=2, sort_by="score")
    for r in results:
        print(f"Title: {r['title']}, Score: {r['score']}")
        
    # Test 2: Content search
    print("\n--- Testing Content Search (党建) ---")
    results = service.search("党建", size=2, sort_by="score")
    for r in results:
        print(f"Title: {r['title']}, Score: {r['score']}")
        if r['highlights']:
            print(f"Highlights: {r['highlights']}")

    # Test 3: Fuzzy filename search
    print("\n--- Testing Fuzzy Search (台帐 -> 台账) ---")
    results = service.search("台帐", size=2, sort_by="score")
    for r in results:
        print(f"Title: {r['title']}, Score: {r['score']}")

if __name__ == "__main__":
    test_search()