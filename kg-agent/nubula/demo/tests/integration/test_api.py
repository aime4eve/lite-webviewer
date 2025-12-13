import requests
import json

url = "http://localhost:5000/api/query"
headers = {"Content-Type": "application/json"}
data = {"keywords": ["智能"], "depth": 2}

try:
    response = requests.post(url, headers=headers, data=json.dumps(data))
    if response.status_code == 200:
        print("Success!")
        print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    else:
        print(f"Failed with status {response.status_code}")
        print(response.text)
except Exception as e:
    print(f"Error: {e}")
