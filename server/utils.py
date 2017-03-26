import json
import time

def read_from_conventional_data_store():
	with open('static/servicesTodo.json') as json_data:
		d = json.loads(json_data.read())
		return d

def add_to_conventional_data_store(room_id, query, mode):
	store = read_from_conventional_data_store()
	item = {}
	item['id'] = len(store)
	item['room_id'] = room_id
	item['query'] = query
	item['mode'] = mode
	item['created_tx'] = time.time()
	item['done'] = 0
	store.append(item)
	with open('static/servicesTodo.json', 'r+') as f:
	    text = f.read()
	    f.seek(0)
	    f.write(json.dumps(store))
	    f.truncate()

def update_on_conventional_data_store(index, mode):
	store = read_from_conventional_data_store()

	if index >= len(store):
		return False

	store[index]['mode'] = mode
	if mode == 5:
		store[index]['done'] = 1
	with open('static/servicesTodo.json', 'r+') as f:
	    text = f.read()
	    f.seek(0)
	    f.write(json.dumps(store))
	    f.truncate()

	return True




def get_room_from_device(device_id):
	return 1
	cursor = db.cursor()

	query = "SELECT id FROM `rooms` WHERE device_id = '%s' LIMIT 1" % (device_id)

	cursor.execute(query)
	row = cursor.fetchone()

	if row:
		return int(row[0])

	return False



def add_request_to_panel(device_id, user_query, mode):
	room_id = get_room_from_device(device_id)
	if not room_id:
		return False

	# database_query = "INSERT INTO `requests` (room_id, request, created_tx, mode) VALUES ('%d', '%s', '%s', '%d')" % (room_id, user_query, time.time(), mode)
	# cursor = db.cursor()
	# try:
	# 	cursor.execute(database_query)
	# 	db.commit()
	# 	# user_id = cursor.lastrowid
	# 	success = True
	# except e:
	# 	db.rollback()
	# 	success = False
	add_to_conventional_data_store(room_id, user_query, mode)

	return True
