def get_room_from_device(db, device_id):
	return 1
	cursor = db.cursor()
	
	query = "SELECT id FROM `rooms` WHERE device_id = '%s' LIMIT 1" % (device_id)
	
	cursor.execute(query)
	row = cursor.fetchone()

	if row:
		return int(row[0])

	return False



def add_request_to_panel(db, device_id, user_query, mode):
	room_id = get_room_from_device(db, device_id)
	if not room_id:
		return False

	database_query = "INSERT INTO `requests` (room_id, request, created_tx, mode) VALUES ('%d', '%s', '%s', '%d')" % (room_id, user_query, time.time(), mode)
	cursor = db.cursor()
	try:
		cursor.execute(database_query)
		db.commit()
		# user_id = cursor.lastrowid
		success = True
	except e:
		db.rollback()
		success = False

	return success
