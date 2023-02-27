const express = require('express');
const request = require('request');
const MongoClient = require('mongodb').MongoClient;

const PORT = 8080;
// our api key for open-weather-map service
const API_KEY_OPEN_WEATHER_MAP = '51e61848b36dfec3fd6759c210361958';

const MONGO_URL = 'mongodb+srv://rlapushin:roee3171999@weather-mobile-app.iaar1av.mongodb.net/?retryWrites=true&w=majority';
const DATABASE_NAME = 'weather-mobile-app';
const USERS_COLLECTION_NAME = 'users';
const client = new MongoClient(MONGO_URL, {
    useNewUrlParser: true,
	useUnifiedTopology: true
});


let app = express();

app.get('/weather', (req, res) => {
	console.log("inside app.get");
	// recieving the lat and lon of the location
    let lat = req.query.lat;
	let lon = req.query.lon;

	if (lat === null || lon === null) {
		return res.status(500).json({err: "please provide valid coordinates!"});
	}

    console.log("recieved lat: " + lat + ", lon: " + lon);

    // calling getWeather function
	getWeather(lat, lon, (err, json) => {
		if(err){
		    console.log("err message: " + err.message);
			return res.status(500).json({err: err.message});
		}
		console.log(json);
		return res.json(json);
	});
});

function getWeather (lat, lon, cb) {
	// for debugging use
	lat = 32.085300;
	lon = 34.781769;
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
					let relevantItem = {
						dateTime: item.dt_txt,
						weather: item.weather[0].description,
						temperature: item.main.temp,
						windSpeed: item.wind.speed,
						icon: item.weather[0].icon
					};
					relevantData.push(relevantItem);
				});
				console.log(relevantData);
				return cb(null, relevantData);
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
		const result = await collection.findOne({userName: userName, password: password});
		console.log("user exists: " + (result !== null));
		return res.json({result: result !== null});
	})
});

app.get('/create-user', (req, res) => {
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
		const result = await collection.insertOne({userName: userName, password: password});
		console.log("result: " + result);
		return res.json({result: result.acknowledged});
	})
});

app.listen(PORT, () => {
    console.log(`Listening on port ${PORT}`);
});

// local URL:
// http://localhost:8080/weather
// Raw Json:
// https://api.openweathermap.org/data/2.5/forecast?lat=32.085300&lon=34.781769&appid=51e61848b36dfec3fd6759c210361958&units=metric
