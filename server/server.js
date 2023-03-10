const express = require('express');
const request = require('request');
const bodyParser = require('body-parser');
const MongoClient = require('mongodb').MongoClient;
const bcrypt = require('bcrypt');

const PORT = 8080;

// our api key for open-weather-map service
const API_KEY_OPEN_WEATHER_MAP = '51e61848b36dfec3fd6759c210361958';

// mongo db setup
const MONGO_URL = 'mongodb+srv://rlapushin:roee3171999@weather-mobile-app.iaar1av.mongodb.net/?retryWrites=true&w=majority';
const DATABASE_NAME = 'weather-mobile-app';
const USERS_COLLECTION_NAME = 'users';
const client = new MongoClient(MONGO_URL, {
    useNewUrlParser: true,
	useUnifiedTopology: true
});

// bcrypt setup
const saltRounds = 10;


let app = express();
app.use(bodyParser.urlencoded({extended : true}));
app.use(bodyParser.json());

app.get('/weather', (req, res) => {
	console.log("inside app.get");
	// receiving the lat and lon of the location
    let lat = req.query.lat;
	let lon = req.query.lon;
	let date = req.query.date;

	if (lat === null || lon === null) {
		return res.status(500).json({err: "please provide valid coordinates!"});
	}

    console.log("received lat: " + lat + ", lon: " + lon + ", date: " + date);

    // calling getWeather function
	getWeather(lat, lon, date, (err, json) => {
		if(err){
		    console.log("err message: " + err.message);
			return res.status(500).json({err: err.message});
		}
		console.log("Got a json to send back..");
		console.log(json);
		return res.json(json);
	});
});

function getWeather (lat, lon, date, cb) {
	let url = `https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${API_KEY_OPEN_WEATHER_MAP}&units=metric`;
    console.log("The request url is: " + url );
    // the request to get the weather
    request.get({url: url, json: true, headers: {'User-Agent': 'request'}}, 
    	(err, res, data) => {
	        if (err) {
	            return cb(err, null);
	        } else {
				// Getting the relevant data from the json
				let relevantData = [];
				data.list.forEach(item => {
					// Only add items with date received
					if (item.dt_txt.startsWith(date)) {
						let dateSplit = item.dt_txt.split(" ")[0];
						let timeSplit = item.dt_txt.split(" ")[1].split(":").slice(0, 2).join(":");
						let relevantItem = {
							date: dateSplit,
							time: timeSplit,
							weather: item.weather[0].description,
							temp: item.main.temp,
							wind: item.wind.speed,
							icon: item.weather[0].icon
						};
						relevantData.push(relevantItem);
					}
				});

				console.log("Data cleaned!");
				return cb(null, {data: relevantData});
	        }
	    
    });
}

app.get('/validate-user', async (req, res) => {
	let userName = req.query.userName || null;
	let password = req.query.password || null;
	if (userName === null || password === null) {
		return res.status(500).json({err: "please provide valid credentials!"});
	}
	console.log("received userName: " + userName + ", password: " + password);

	client.connect()
	.then(async () => {
		console.log("connected to database!");
		const collection = client.db(DATABASE_NAME).collection(USERS_COLLECTION_NAME);
		const user = await collection.findOne({userName: userName});
		console.log("user exists: " + (user !== null), user);
		client.close();

		bcrypt.compare(password, user.password, (err, result) => {
			if (err) {
				return res.json({result: false, markers: []});
			} else if (user.markers === null) {
				return res.json({result: result, markers: []});
			}

			return res.json({result: result, markers: user.markers});
		});
	});
});

app.get('/create-user', (req, res) => {
	let userName = req.query.userName || null;
	let password = req.query.password || null;
	if (userName === null || password === null) {
		return res.status(500).json({err: "please provide valid credentials!"});
	}
	console.log("received userName: " + userName + ", password: " + password);

	bcrypt.hash(password, saltRounds, (err, hash) => {
		console.log("hash: " + hash);
		client.connect()
		.then(async () => {
			console.log("connected to database!");
			const collection = client.db(DATABASE_NAME).collection(USERS_COLLECTION_NAME);
			const result = await collection.insertOne({userName: userName, password: hash, markers: []});
			console.log("result: " + result);
			client.close();
			return res.json({result: result.acknowledged, markers: []});
		})
	});
});

app.post('/update-user-markers', (req, res) => {
	console.log("received post request to update user markers");
	console.dir(req.body);
	let userName = req.body.userName || null;
	let markers = req.body.markers || null;

	if (userName === null || markers === null) {
		console.log("invalid data!");
		return res.status(500).json({err: "please provide valid data!"});
	}

	client.connect()
	.then(async () => {
		console.log("connected to database!");
		const collection = client.db(DATABASE_NAME).collection(USERS_COLLECTION_NAME);
		const result = await collection.updateOne(
			{userName: userName},
			{$set: {markers: markers}}
		)
		console.log("result: " + result);
		client.close();
		return res.json({result: result.acknowledged});
	});
});

app.listen(PORT, () => {
    console.log(`Listening on port ${PORT}`);
});
