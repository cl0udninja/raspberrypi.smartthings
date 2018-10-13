/**
 *  MagicHome LED Controller Discovery
 *
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
		name: "MagicHome LED Controller Discovery",
		namespace: "cl0udninja",
		author: "cl0udninja",
		description: "MagicHome LED Controller Discovery",
		category: "My Apps",
		iconUrl: "https://storage.googleapis.com/storage.cl0ud.ninja/MagicHomeLogo.jpg",
		iconX2Url: "https://storage.googleapis.com/storage.cl0ud.ninja/MagicHomeLogo.jpg",
		iconX3Url: "https://storage.googleapis.com/storage.cl0ud.ninja/MagicHomeLogo.jpg")

preferences {
	 page(name:"restApiSetup", title:"RestAPI Gateway settings", nextPage:"devices", uninstall:false) {
        section() {
            paragraph image: "https://storage.googleapis.com/storage.cl0ud.ninja/MagicHomeLogo.jpg",
            	title: "RestAPI Gateway",
                "Enter the IP address and port (ip:port) format of the RestAPI Gateway"
            input "restControllerIP", "text", title:"RestAPI IP", defaultValue: "192.168.1.40"
            input "restControllerPort", "number", title:"RestAPI Port", defaultValue: 8080
        }
    }
    page name: "deviceList", title: "Discovered Devices", install: true, uninstall: true
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    initialize()
}

def initialize() {
	unsubscribe()
}

def deviceList() {
	log.debug('lofasz')
    
    dynamicPage(name: "deviceList") {
	    section("Automation name") {
            label title: "Enter custom name", required: false
        }
    }
       
}

private devices2() {
	if (restControllerIp == null || restControllerIp.equals("")) return
    if (restControllerPort == null || restControllerPort.equals("")) return
	def iphex = convertIPtoHex(restControllerIp)
    def porthex = convertPortToHex(restControllerPort)

    def uri = "/api/magic"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        "${ipHex}:${portHex}",
        [callback: parse]
    )
    log.debug "Getting FlowerCare data ${hubAction}"
    hubAction
}

private parse(description) {
	log.debug "Parse ${description}"
    if (!description.hasProperty("body")) {
    	log.debug "Skipping parse"
        return
    }
    log.debug "Parsing '${description?.body}'"
	def msg = parseLanMessage(description?.body)
    log.debug "Msg ${msg}"
	def json = parseJson(description?.body)
    log.debug "JSON '${json}'"
    
    if (json == null) return
    
    state.controllers = []
    json.length.times {
    	state.controllers.push(json)
    }
    
    log.debug(state.controllers)
}


private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}