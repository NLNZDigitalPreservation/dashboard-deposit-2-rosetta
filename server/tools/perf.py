from datetime import datetime as dt
import time

if __name__ == "__main__":
    sessions = {}
    url = "http://localhost:7071/runtime/webhooks/durabletask/instances/6ef83398-7610-4f99-bf11-d44772797db1?code=PFaQsr_l5-fLZ-MV9wpnuZiFhqNE-ClDCLURr9S7ExJ8AzFuIXfgPA=="

    for i in range(100 * 1024 * 1024):
        sessions[i] = (i, dt.now().timestamp(), url)
        if i % (1024 * 1024) == 0:
            print(f"Added {i} entries")

    print("All done")
    time.sleep(600)
