from datetime import datetime as dt
import time

if __name__ == "__main__":
    sessions = {}
    url = "http://example.com/some/very/long/url/that/uses/memory"
    for i in range(100 * 1024 * 1024):
        sessions[i] = (i, dt.now().timestamp(), url)
        if i % (1024 * 1024) == 0:
            print(f"Added {i} entries")

    print("All done")
    time.sleep(600)
