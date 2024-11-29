const fs = require("fs");
const {Readable} = require("stream");
const {finished} = require("stream/promises");

async function downloadYear(year) {
	const directoryName = `./${year}`;
	try {
		if(!fs.existsSync(directoryName)) {
			fs.mkdirSync(directoryName);
			console.log(`Created ${directoryName}`);
		}
	} catch (error) {
		console.log(error);
	}
	let response = await fetch(`https://scotusapi.onrender.com/cases?year=${year}`);
	response = await response.json();
	if(response) {
		for(c of response.cases) {
			const name = c.title;
			console.log(`Downloading ${name}`);
			const url = c.transcript;
			const { body } = await fetch(url);
			const fileStream = fs.createWriteStream(`./${year}/${name}.pdf`);
			await finished(Readable.fromWeb(body).pipe(fileStream));
		}
	}
}
downloadYear(2013);