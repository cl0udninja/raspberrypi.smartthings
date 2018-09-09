/**
 *  Quietcool Whole House Fan
 *  Licensed under the GNU v3 (https://www.gnu.org/licenses/gpl-3.0.en.html)
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
preferences {		
	input("ip", "string", title:"IP Address", description: "192.168.1.150", defaultValue: "192.168.1.150" ,required: true, displayDuringSetup: true)		
	input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)		
}
metadata {
	definition (name: "Quietcool Whole House Fan", namespace: "cl0udninja", author: "Janos Elohazi") {
		capability "Polling"
		capability "Refresh"
		capability "Actuator"
		capability "Switch"
        capability "Indicator"
	}
    
    command "fanLow"
    command "fanHigh"

	simulator {
		// TODO: define status and reply messages here
	}

	tiles() {
    	multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 3, canChangeIcon: true) {
			tileAttribute ("device.currentState", key: "PRIMARY_CONTROL") {
				attributeState "OFF", label:'${name}', action:"switch.on", icon:"st.Lighting.light24"
				attributeState "LOW", label:'${name}', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#00e676"
                attributeState "HIGH", label:'${name}', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#FF3D00"
			}
    	}
        standardTile("low", "device.currentState", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "default", label: "LOW", action:"fanLow", icon: "st.Home.home30", backgroundColor:"#00e676", nextState: "LOW"
        }
        standardTile("high", "device.currentState", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "default", label: "HIGH", action:"fanHigh", icon: "st.Home.home30", backgroundColor:"#FF3D00", nextState: "HIGH"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "switch"
        
        details(["switch", "low", "high", "refresh"])
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
	sendEvent(name: "checkInterval", value: 60 * 10, data: [protocol: "cloud"], displayed: false)
    refresh()
}

def off() {
	setFan("OFF")
}

def on() {
	fanHigh()
}

def fanLow() {
	setFan("LOW")
}

def fanHigh() {
	setFan("HIGH")
}

def setFan(level) {
	log.debug("setFan ${level}");
	def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    
    def uri = "/api/fan"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    headers.put("Content-type", "application/json")
    def body = "{\"fanSpeed\":\"${level}\"}"
   	sendHubCommand(new physicalgraph.device.HubAction(
        method: "POST",
        path: uri,
		headers: headers,
        body: body,
        "${ipHex}:${portHex}"
    ))
    sendEvent(name: "currentState", value: level)
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
    log.debug "JSON '${json.fanSpeed}'"
    
    if (json.containsKey("fanSpeed")) {
    	sendEvent(name: "currentState", value: json.fanSpeed)
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getFanState()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getFanState()
}

private getFanState() {
	log.debug "getFanState"
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    
    def uri = "/api/fan"
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
    log.debug "Getting Fan data ${hubAction}"
    hubAction 
}

def sync(ip, port) {
	log.debug "sync ${ip} ${port}"
	def existingIp = getDataValue("ip")
	def existingPort = getDataValue("port")
	if (ip && ip != existingIp) {
		updateDataValue("ip", ip)
	}
	if (port && port != existingPort) {
		updateDataValue("port", port)
	}
    def ipHex = convertIPToHex(ip)
    def portHex = convertPortToHex(port)
    device.deviceNetworkId = "${ipHex}:${portHex}"
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