import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import datetime
import json
import requests

server_key = 'AAAAsikYyb0:APA91bGwXYmYutp5dyUI74YNndwBgEFWfCmB4RxJdiPSHqxZ8Xa6fy_4MXKbPbTcbXoIdNMB5G0-CWXlT_fTzUUMPYcPIJ-TL7_UzPhFYSQW8pjJpUZIbvGuqxYKwiFQfq8jKFeUmTyi'
serial = '123'
device_token = ''

def send_notification(msg) :
	headers = {
		'Authorization': 'key= ' + server_key,
		'Content-Type': 'application/json',
	}

	data = {
		'to': device_token,
		'notification': {
			'title': 'Inner car',
			'body': msg
		},
	}

	response = requests.post('https://fcm.googleapis.com/fcm/send', headers=headers, data=json.dumps(data))
	print(response)
	print("")
	print("")
	print("exit")

if __name__ == '__main__':
	cred = credentials.Certificate('capstone-liunx0-firebase-adminsdk-8ke8r-eca629c61b.json')

	firebase_admin.initialize_app(cred, {
		'databaseURL': 'https://capstone-liunx0.firebaseio.com/'
		})

	ref = db.reference(serial + '/Device')
	device_token = ref.get()

	print("")
	print("")
	print(device_token)
	print("")
	print("")

	if(device_token != None) :
		send_notification('baby')

