from flask import Flask, request, render_template, send_from_directory
from flask_cors import CORS
import json

from utils import add_request_to_panel, add_to_conventional_data_store as add, update_on_conventional_data_store as update
from config import HOSTNAME, USERNAME, PASSWORD, DATABASE


app = Flask(__name__, static_url_path='')
CORS(app)

@app.route('/', methods=['GET'])
def main():
	return "Hello world", 200


@app.route('/services', methods=['GET', 'POST'])
def service():
	if request.method == 'POST':
		device_id = request.args.get("device")
		query = request.args.get("query")

		if add_request_to_panel(device_id, query, 2):
			return "ok"

		return "not ok"
	else if request.method == 'GET':
		return send_from_directory('static', 'servicesTodo.json')

@app.route('/update/services', methods=['GET'])
def update_services():
	req_id = int(request.args.get("id"))
	mode = int(request.args.get("mode"))

	if update(req_id, mode):
		return "ok"

	return "not ok"


if __name__ == "__main__":
	app.run(debug=True, host="0.0.0.0", threaded=True)
