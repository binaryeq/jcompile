const convert = require('xml-js');
const fs = require("fs");

const existingPluginsListsWithoutCompilerPlugin = [];
const existingCompilerPlugins = [];

console.warn("Starting.");
const stdinBuffer = fs.readFileSync(0); // STDIN_FILENO = 0
const stdin = stdinBuffer.toString();

let pomJson = convert.xml2js(stdin, { compact: false, spaces: 2 });
//const newPomJson = transform(pomJson, []);
console.warn(`BEFORE: pomJson=`, pomJson);
transform(pomJson, []);
inject();
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
			existingCompilerPlugins.push(ancestors[0]);
			const iParentPluginsList = existingPluginsListsWithoutCompilerPlugin.indexOf(ancestors[1]);
			if (iParentPluginsList !== -1) {
				console.warn(`Removing its parent from the list of <plugins> elements without a compiler plugin.`);
				existingPluginsListsWithoutCompilerPlugin.splice(iParentPluginsList, 1);
			}
		}
	}

	if (pomJson.type === 'element' && pomJson.name === 'plugins' && ancestors[0]?.type === 'element' && ancestors[0]?.name === 'build') {
		console.warn(`Found existing <plugins> element within a <build> element!`);
		existingPluginsListsWithoutCompilerPlugin.push(pomJson);
	}

	if ('elements' in pomJson) {
		for (const e of pomJson.elements) {
			transform(e, [pomJson, ...ancestors]);
		}
	}
}

function findOrMakeElement(root, elemName) {
	console.warn(`findOrMakeElement(root=`, root, `, elemName=${elemName}) called.`);		//DEBUG
	if (!('elements' in root)) {
		console.warn(`findOrMakeElement(): Adding nonexistent 'elements' array`);		//DEBUG
		root.elements = [];
	}

	let existingElem = root.elements.find((e) => e.type === 'element' && e.name === elemName);
	if (existingElem) {
		console.warn(`findOrMakeElement(): Found existing '${elemName}', returning it.`);		//DEBUG
		return existingElem;
	} else {
		console.warn(`findOrMakeElement(): Did not find any existing '${elemName}', adding a new one.`);		//DEBUG
		const elem = { type: 'element', name: elemName };
		root.elements.push(elem);
		return elem;
	}
}

function forEachElement(root, elemNames, cb) {
	if (!('elements' in root)) {
		return;
	}

	if (!elemNames.length) {
		cb(root);
	}

	const elemName = elemNames.shift();
	for (const next of root.elements.filter((e) => e.type === 'element' && e.name === elemName)) {
		forEachElement(next, elemNames, cb);
	}
}

function inject() {
	console.warn(`Will inject ${existingPluginsListsWithoutCompilerPlugin.length} maven-compiler-plugin plugins into existing <plugins> elements that don't have them yet:`);
	for (const e of existingPluginsListsWithoutCompilerPlugin) {
		e.elements.push({ type: 'element', name: 'plugin', elements: [
			{
				type: 'element',
				name: 'plugin',
				elements: [
					{
						type: 'element',
						name: 'artifactId',
						elements: [
							{
								type: 'text',
								text: 'maven-compiler-plugin'
							}
						]
					},
					{
						type: 'element',
						name: 'configuration',
						elements: [
							{
								type: 'element',
								name: 'compilerArgs',
								elements: [
									{
										type: 'element',
										name: 'arg',
										elements: [
											{
												type: 'text',
												text: '-g'
											}
										]
									}
								]
							}
						]
					}
				]
			}
		] });
	}

	console.warn(`Will modify ${existingCompilerPlugins.length} maven-compiler-plugin <plugin> elements by adding '-g' arguments:`);

	function addHyphenG(e) {
		console.warn(`addHyphen(e=`, e, `) called.`);
		const configuration = findOrMakeElement(e, 'configuration');
		const compilerArgs = findOrMakeElement(configuration, 'compilerArgs');
		if (!('elements' in compilerArgs)) {
			console.warn(`addHyphen(): Adding missing elements array.`);
			compilerArgs.elements = [];
		}
		compilerArgs.elements.push({ type: 'element', name: 'arg', elements: [{ type: 'text', text: '-g' }] });
		console.warn(`addHyphen() about to return: `, compilerArgs);
	}

	for (const e of existingCompilerPlugins) {
		addHyphenG(e);

		// Also need to check for <configuration>s within <executions><execution>...</execution></executions>
		forEachElement(e, ['executions', 'execution'], (e2) => {
			addHyphenG(e2);
		});
	}
}
