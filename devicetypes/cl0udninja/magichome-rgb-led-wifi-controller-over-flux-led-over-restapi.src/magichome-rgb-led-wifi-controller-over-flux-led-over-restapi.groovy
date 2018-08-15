/**
 *  Raspberry Pi flux_led (https://github.com/Danielhiversen/flux_led) over RestAPI Device Handler

 *  Licensed under the GNU v3 (https://www.gnu.org/licenses/gpl-3.0.en.html)
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
preferences {		
	input("controllerIp", "string", title:"IP Address of the REST server", description: "IP Address of the REST server", defaultValue: "192.168.1." ,required: true, displayDuringSetup: true)		
	input("port", "string", title:"Port of the REST server", description: "Port of the REST server", defaultValue: "8080" , required: true, displayDuringSetup: true)
    input("ip", "string", title:"IP of the MagicHome controller", description: "IP of the MagicHome controller", defaultValue: "192.168.1." , required: true, displayDuringSetup: true)
}
metadata {
	definition (name: "MagicHome RGB LED WiFi Controller over Flux_LED over RestAPI", namespace: "cl0udninja", author: "cl0udninja") {
        capability "Switch Level"
        capability "Actuator"
        capability "Color Control"
        capability "Switch"
        capability "Refresh"
        capability "Sensor"
        capability "Polling"
	}
    
    tiles(scale: 2)  {
    	multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 3, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Seasonal Winter.seasonal-winter-011", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Seasonal Winter.seasonal-winter-011", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Seasonal Winter.seasonal-winter-011", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Seasonal Winter.seasonal-winter-011", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.color", key: "SECONDARY_CONTROL") {
				attributeState "color", label:'Color${currentValue}'
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
    	}
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch", "refresh"])
    }
}   

def installed() {
	log.debug "installed"
	initialize();
}

def updated() {
	log.debug "updated"
	initialize();
}

def ping() {
	log.debug "ping"
	poll()
}

def initialize() {
	log.debug "initialize"
    unschedule()
	sendEvent(name: "checkInterval", value: 60 * 10, data: [protocol: "cloud"], displayed: false)
    refresh()
}

// parse events into attributes
def parse(description) {
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
   
    sendEvent(name: "switch", value: json.status)
}

def parseFull(description) {
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
   
    sendEvent(name: "switch", value: json.status)
	sendEvent(name: "color", value: convertRGBtoHex(json.red, json.green, json.blue));
    sendEvent(name: "level", value: 100);
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getLED()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getLEDFull()
}

def off() {
	def iphex = convertIPtoHex(controllerIp)
    def porthex = convertPortToHex(port)
    def currentColor = getEffectiveColor()
    def r = currentColor ? Integer.parseInt(currentColor.substring(1, 3), 16) : 0;
    def g = currentColor ? Integer.parseInt(currentColor.substring(3, 5), 16) : 0;
    def b = currentColor ? Integer.parseInt(currentColor.substring(5, 7), 16) : 0;
    
    def uri = "/api/magic"
    def headers=[:]
    headers.put("HOST", "${controllerIp}:${port}")
    headers.put("Accept", "application/json")
    headers.put("Content-type", "application/json")
    def body = "{\"status\": \"OFF\",\"red\": ${r},\"green\": ${g},\"blue\": ${b},\"ip\": \"${ip}\"}"
    log.debug(body)
   	def hubAction = sendHubCommand(new physicalgraph.device.HubAction(
        method: "PUT",
        path: uri,
		headers: headers,
        body: body,
        "${ipHex}:${portHex}",
        [callback: parse]
    ))
    log.debug "Turning off LED  ${hubAction}"
    hubAction
}

def on() {
	def iphex = convertIPtoHex(controllerIp)
    def porthex = convertPortToHex(port)
    def currentColor = getEffectiveColor()
    def r = currentColor ? Integer.parseInt(currentColor.substring(1, 3), 16) : 0;
    def g = currentColor ? Integer.parseInt(currentColor.substring(3, 5), 16) : 0;
    def b = currentColor ? Integer.parseInt(currentColor.substring(5, 7), 16) : 0;
    
    def uri = "/api/magic"
    def headers=[:]
    headers.put("HOST", "${controllerIp}:${port}")
    headers.put("Accept", "application/json")
    headers.put("Content-type", "application/json")
    def body = "{\"status\": \"ON\",\"red\": ${r},\"green\": ${g},\"blue\": ${b},\"ip\": \"${ip}\"}"
    log.debug(body)
   	def hubAction = sendHubCommand(new physicalgraph.device.HubAction(
        method: "PUT",
        path: uri,
		headers: headers,
        body: body,
        "${ipHex}:${portHex}",
        [callback: parse]
    ))
    log.debug "Turning off LED  ${hubAction}"
    hubAction
}

def setLevel(level) {
	log.trace "setLevel($level)"
    sendEvent(name: "level", value: level);
    if (level == 0) {
    	off()
    } else {
        on()
    }
}

def setColor(value) {
	log.debug(value)
	sendEvent(name: "color", value: value.hex);
	on();
}

private getEffectiveColor() {
	def currentColor = device.latestValue("color")
	def r = currentColor ? Integer.parseInt(currentColor.substring(1, 3), 16) : 0;
    def g = currentColor ? Integer.parseInt(currentColor.substring(3, 5), 16) : 0;
    def b = currentColor ? Integer.parseInt(currentColor.substring(5, 7), 16) : 0;
    
    def currentLevel = device.latestValue("level")
    r = r * currentLevel / 100;
    g = g * currentLevel / 100;
    b = b * currentLevel / 100;
    return convertRGBtoHex(r, g, b)
}

private getLEDFull() {
	def iphex = convertIPtoHex(controllerIp)
    def porthex = convertPortToHex(port)

    def uri = "/api/magic?ip=${ip}"
    def headers=[:]
    headers.put("HOST", "${controllerIp}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        "${ipHex}:${portHex}",
        [callback: parseFull]
    )
    log.debug "Getting LED data ${hubAction}"
    hubAction
}

private getLED() {
	def iphex = convertIPtoHex(controllerIp)
    def porthex = convertPortToHex(port)

    def uri = "/api/magic?ip=${ip}"
    def headers=[:]
    headers.put("HOST", "${controllerIp}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        "${ipHex}:${portHex}",
        [callback: parse]
    )
    log.debug "Getting LED data ${hubAction}"
    hubAction
}

private convertRGBtoHex(r, g, b) {
	return String.format("#%02x%02x%02x", Math.round(r), Math.round(g), Math.round(b)); 
}

private String convertIPtoHex(ipAddress) {
	log.debug "convertIPtoHex ${ipAddress} to hex"
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	log.debug "convertPortToHex ${port} to hex"
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}