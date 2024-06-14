const convert = require('xml-js');
const fs = require("fs");

console.warn("Starting.");
const stdinBuffer = fs.readFileSync(0); // STDIN_FILENO = 0
const stdin = stdinBuffer.toString();

let pomJson = convert.xml2js(stdin, { compact: false, spaces: 2 });
//const newPomJson = transform(pomJson, []);
console.warn(`BEFORE: pomJson=`, pomJson);
transform(pomJson, []);
console.warn(`AFTER: pomJson=`, pomJson);
const newPomJson = pomJson;		//HACK
let newPomXml = convert.js2xml(newPomJson, { compact: false, spaces: 2 });
console.log(newPomXml);
console.warn("Finished.");

function transform(pomJson, ancestors) {
	//console.warn(`transform(pomJson=`, pomJson, `, ancestors=[${ancestors.map((e) => e.name).join(', ')}]) called.`);		//DEBUG
	if (pomJson.type === 'element' && pomJson.name === 'artifactId' && ancestors.slice(0, 3).every((e) => e.type === 'element') && ancestors.slice(0, 3).map((e) => e.name).join('>') === 'plugin>plugins>build') {
		if (pomJson?.elements?.[0]?.text === 'maven-compiler-plugin') {
			console.warn(`Found existing maven-compiler-plugin element!`);
			//TODO
		}
	}

	if ('elements' in pomJson) {
		for (const e of pomJson.elements) {
			transform(e, [pomJson, ...ancestors]);
		}
	}
}
