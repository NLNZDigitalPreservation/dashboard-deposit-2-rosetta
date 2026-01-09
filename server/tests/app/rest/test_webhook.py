import falcon
from falcon import testing
import random
from app.rest import resources


def test_resource_webhook(peewee_db):
    app = falcon.App()
    res_webhook = resources.WebhookResource()
    app.add_route("/webhook", res_webhook)
    client = testing.TestClient(app)

    rand_entity = random.randint(48238455, 93945934)
    version = 4

    blob_event = [
        {
            "topic": "/subscriptions/{subscription-id}/resourceGroups/{rg}/providers/Microsoft.Storage/storageAccounts/{account}",
            "subject": "/blobServices/default/containers/mycontainer/blobs/myfile.txt",
            "eventType": "Microsoft.Storage.BlobCreated",
            "id": "e8d88506-001e-00b2-51b2-123456789abc",
            "data": {
                "api": "PutBlob",
                "clientRequestId": "abc12345-6789-4def-0123-456789abcdef",
                "requestId": "12345678-0001-0042-89ab-123456abcdef",
                "eTag": "0x8D123456789ABCDE",
                "contentType": "text/plain",
                "contentLength": 524288,
                "blobType": "BlockBlob",
                "url": f"http://localhost:10000/devstoreaccount1/fixity-dev/IE41361819/V{version}-IE{rand_entity}.xml",
                "sequencer": "00000000000000000000000000001234",
                "storageDiagnostics": {"batchId": "e3b0c442-98fc-4623-b889-abcdef123456"},
            },
            "dataVersion": "",
            "metadataVersion": "1",
            "eventTime": "2025-09-18T02:15:22.123456Z",
        },
        {
            "topic": "/subscriptions/{subscription-id}/resourceGroups/{rg}/providers/Microsoft.Storage/storageAccounts/{account}",
            "subject": "/blobServices/default/containers/mycontainer/blobs/myfile.txt",
            "eventType": "Microsoft.Storage.BlobCreated",
            "id": "e8d88506-001e-00b2-51b2-123456789abc",
            "data": {
                "api": "PutBlob",
                "clientRequestId": "abc12345-6789-4def-0123-456789abcdef",
                "requestId": "12345678-0001-0042-89ab-123456abcdef",
                "eTag": "0x8D123456789ABCDE",
                "contentType": "text/plain",
                "contentLength": 524288,
                "blobType": "BlockBlob",
                "url": f"http://localhost:10000/devstoreaccount1/fixity-dev/IE41361819/V{version}-FL{rand_entity}.xml",
                "sequencer": "00000000000000000000000000001234",
                "storageDiagnostics": {"batchId": "e3b0c442-98fc-4623-b889-abcdef123456"},
            },
            "dataVersion": "",
            "metadataVersion": "1",
            "eventTime": "2025-09-18T02:15:22.123456Z",
        },
    ]

    resp = client.simulate_post("/webhook", json=blob_event)

    assert resp.status == falcon.HTTP_OK
    print(resp.text)
