/**
 *  Raspberry Pi Monitor
 *
 *  Monitor your Raspberry Pi using SmartThings and Raspberry Pi Monitor <https://github.com/cl0udninja/raspberrypi.monitor>
 *
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
	definition (name: "Raspberry Pi Monitor (Manual IP)", namespace: "cl0udninja", author: "Janos Elohazi") {
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Sensor"
        capability "Health Check"
        
        attribute "cpuFrequency", "number"       
        attribute "freeMemory", "number"
        attribute "cpuCoreVoltage", "number"
        attribute "modelName", "string"
        attribute "boardType", "string"
        attribute "javaVersion", "string"
        attribute "hostname", "string"
        attribute "serialNumber", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("cpuTemperature", "device.cpuTemperature", width: 6, height: 4, canChangeIcon: true) {
            state "cpuTemperature", label:'${currentValue}°',
			icon: "http://storage.googleapis.com/storage.cl0ud.ninja/raspberry-pi-logo.png"
			backgroundColors:[
            	[value: 25, color: "#153591"],
                [value: 35, color: "#1e9cbb"],
                [value: 47, color: "#90d2a7"],
                [value: 59, color: "#44b621"],
                [value: 67, color: "#f1d801"],
                [value: 76, color: "#d04e00"],
                [value: 77, color: "#bc2323"]
            ]
        }
        valueTile("freeMemoryLabel", "device.label.freeMemory", width: 3, height: 1) {
        	state "default", label:'Free memory'
        }
        valueTile("freeMemory", "device.freeMemory", width: 2, height: 1) {
        	state "default", label:'${currentValue}\nMB'
        }
        valueTile("freeMemoryPercent", "device.freeMemoryPercent", width: 1, height: 1) {
            state "freeMemoryPercent", label:'${currentValue}%',
            backgroundColors:[
                [value: 0, color: "#44b621"],
                [value: 50, color: "#44b621"],
                [value: 70, color: "#f1d801"],
                [value: 80, color: "#d04e00"],
                [value: 90, color: "#bc2323"]
            ]
        }
        valueTile("cpuCoreVoltage", "device.cpuCoreVoltage", width: 2, height: 2) {
        	state "default", label:'CPU:\n${currentValue} V',
            backgroundColors:[
                [value: 1.1, color: "#bc2323"],
                [value: 1.2, color: "#44b621"],
                [value: 1.3, color: "#bc2323"]
            ]
        }
		valueTile("modelName", "device.modelName", width: 2, height:1) {
        	state "default", label:'Model name:\n${currentValue}'
        }
        valueTile("boardType", "device.boardType", width: 2, height:1) {
        	state "default", label:'Board type:\n${currentValue}'
        }
        valueTile("javaVersion", "device.javaVersion", width: 2, height:1) {
        	state "default", label:'Java version:\n${currentValue}'
        }
        valueTile("hostname", "device.hostname", width: 2, height:1) {
        	state "default", label:'Hostname:\n${currentValue}'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "freeMemoryPercent"
        
        details(["cpuTemperature", "freeMemoryLabel", "freeMemory", "freeMemoryPercent", "cpuCoreVoltage", "modelName", "boardType", "javaVersion", "hostname", "refresh"])
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
// parse events into attributes
def parse(description) {
    log.debug "Parsing '${description?.json}'"
	def msg = parseLanMessage(description?.body)
    log.debug "Msg ${msg}"
	def json = parseJson(description?.body)
    log.debug "JSON '${json}'"
    
    if (json.containsKey("cpuTemperature")) {
    	if (getTemperatureScale() == "C") {
	    	sendEvent(name: "cpuTemperature", value: json.cpuTemperature)
        } else {
        	def fahrenheit = json.cpuTemperature * 9 / 5 + 32
            sendEvent(name: "cpuTemperature", value: fahrenheit)
        }
    }
    if (json.containsKey("freeMemory")) {
    	sendEvent(name: "freeMemory", value: (json.freeMemory/1024/1024).toDouble().round(2))
        if (json.containsKey("totalMemory")) {
        	
        	sendEvent(name: "freeMemoryPercent", value: (json.freeMemory/json.totalMemory*100).toDouble().round())
        }
    }
    if (json.containsKey("cpuCoreVoltage")) {
    	sendEvent(name: "cpuCoreVoltage", value: json.cpuCoreVoltage)
    }
    if (json.containsKey("modelName")) {
    	sendEvent(name: "modelName", value: json.modelName)
    }
    if (json.containsKey("boardType")) {
    	sendEvent(name: "boardType", value: json.boardType)
    }
    if (json.containsKey("javaVersion")) {
    	sendEvent(name: "javaVersion", value: json.javaVersion)
    }
    if (json.containsKey("hostname")) {
    	sendEvent(name: "hostname", value: json.hostname)
    }
    if (json.containsKey("serialNumber")) {
    	sendEvent(name: "serialNumber", value: json.serialNumber)
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getPiInfo()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getPiInfo()
}

private getPiInfo() {
	def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    
	def uri = "/api/pi"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        "${iphex}:${porthex}",
        [callback: parse]
    )
    log.debug "Getting Pi data ${hubAction}"
    hubAction 
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

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
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