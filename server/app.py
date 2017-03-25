from flask import Flask, request, render_template
from flask_cors import CORS

from utils import add_request_to_panel
from config import HOSTNAME, USERNAME, PASSWORD, DATABASE


app = Flask(__name__)
CORS(app)

db = MySQLdb.connect(
	host=HOSTNAME,
	user=USERNAME,
	passwd=PASSWORD,
	db=DATABASE
)
# cursor = db.cursor()

# @app.route('/food', methods=['GET'])
# def food():


@app.route('/service', methods=['GET'])
def service():
	device_id = request.args.get("device")
	query = request.args.get("query")

	add_request_to_panel(db, device_id, query, 2)


@app.route('/', methods=['GET'])
def main():
	return "Hello world", 200


if __name__ == "__main__":
	app.run(debug=True, host="0.0.0.0", threaded=True)

