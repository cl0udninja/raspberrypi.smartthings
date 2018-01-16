preferences {
        input("ip", "string", title:"IP Address", description: "192.168.1.150", defaultValue: "192.168.1.150" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Raspberry Pi", namespace: "cl0udninja", author: "Janos Elohazi") {
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Sensor"
        
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
            state "cpuTemperature", label:'${currentValue}°C',
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
        valueTile("cpuFrequencyLabel", "device.label.cpuFrequency", width: 4, height: 1) {
        	state "default", label:'CPU'
        }
        valueTile("cpuFrequency", "device.cpuFrequency", width: 2, height: 1) {
        	state "default", label:'${currentValue}\nMHz'
        }
        valueTile("freeMemoryLabel", "device.label.freeMemory", width: 4, height: 1) {
        	state "default", label:'Free memory'
        }
        valueTile("freeMemory", "device.freeMemory", width: 2, height: 1) {
        	state "default", label:'${currentValue}\nMB'
        }
        valueTile("freeMemoryPercentLabel", "device.label.freeMemoryPercent", width: 4, height: 1) {
        	state "default", label:'Free memory'
        }
        valueTile("freeMemoryPercent", "device.freeMemoryPercent", width: 2, height: 1) {
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
        
        main "cpuTemperature"
        
        details(["cpuTemperature", "cpuFrequencyLabel", "cpuFrequency", "freeMemoryLabel", "freeMemory", "freeMemoryPercentLabel", "freeMemoryPercent", "cpuCoreVoltage", "modelName", "boardType", "javaVersion", "hostname", "refresh"])
    }
}

// parse events into attributes
def parse(description) {
    log.debug "Parsing '${description.json}'"
	def msg = parseLanMessage(description.body)
    log.debug "Msg ${msg}"
	def json = parseJson(description.body)
    log.debug "JSON '${json}'"
    
    if (json.containsKey("cpuTemperature")) {
    	sendEvent(name: "cpuTemperature", value: json.cpuTemperature)
    }
    if (json.containsKey("cpuFrequency")) {
    	sendEvent(name: "cpuFrequency", value: json.cpuFrequency/1000/1000)
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
    getRPiData()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getRPiData()
}

private getRPiData() {
	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex:$porthex"
    
    log.debug "Device network id: ${device.deviceNetworkId}"
    
	def uri = "/api/pi"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        device.deviceNetworkId,
        [callback: parse]
    )
    log.debug "Getting Pi data ${hubAction}"
    hubAction 
}

private String convertIPtoHex(ipAddress) {
	log.debug "converting ${ip} to hex"
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	log.debug "converting ${port} to hex"
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}