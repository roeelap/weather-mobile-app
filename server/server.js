const express = require('express');
const request = require('request');

const PORT = 8080;
// our api key for openweathermap service
const API_KEY_OPEN_WEATHER_MAP = '51e61848b36dfec3fd6759c210361958';

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
	let cnt = 16; // The numbers of days to get forcast for - 16 is the maximun
	let url = `api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${API_KEY_OPEN_WEATHER_MAP}&units=metric`;
    // let url = `https://api.openweathermap.org/data/2.5/forecast/daily?lat=${lat}&lon=${lon}&cnt=${cnt}&appid=${API_KEY_OPEN_WEATHER_MAP}&units=metric`;
    console.log("The request url is: " + url );
    // the request to get the weather
	//TODO: <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    request.get({url: url, json: true, headers: {'User-Agent': 'request'}}, 
    	(err, res, data) => {
	        if (err) {
	            return cb(err, null);
	        } else {
				// Getting the relevant data from the json
				let metaData = data["Meta Data"];
				let timeSeries = data["Time Series (5min)"];
				let latestTime = metaData["3. Last Refreshed"];
				let latestData = timeSeries[latestTime];
				let price = latestData["4. close"];
				return cb(null, {"price": price});
	        }
	    
    });
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}

app.listen(PORT, () => {
    console.log(`Listening on port ${PORT}`);
});

// local URL:
// http://localhost:8080/weather
