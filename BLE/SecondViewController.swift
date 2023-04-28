//
//  SecondViewController.swift
//  BLE
//
//  Created by Anh Đức on 22/04/2023.
//

import UIKit
import CoreBluetooth


class SecondViewController: UIViewController, CBCentralManagerDelegate {
    var linkLocal: String?
    var userName: String?
    var password: String?
    var apikey: String?
    var roomNumber: String?
    var centralManager: CBCentralManager!
    var myCharacteristic: CBCharacteristic?
    var stringNameDevice = ""
    var keyaccess = ""
    var peri: CBPeripheral?
    
    @IBOutlet weak var labelStatus: UILabel!
    
    @IBOutlet weak var getOpenKey: UIButton!
    @IBOutlet weak var getCallKey: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.getRoomNumber(self.userName ?? "", self.password ?? "")
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        getOpenKey.tintColor = UIColor.black
        getCallKey.tintColor = UIColor.black
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            central.scanForPeripherals(withServices: nil, options: nil)
            labelStatus.text = "접속중"
        } else {
            print("Bluetooth not available.")
        }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print("Peripheral name: \(peripheral.name ?? "Unknown"), RSSI: \(RSSI)")
        var nameDevice = self.stringNameDevice
        var name = peripheral.name ?? ""
        self.peri = peripheral
        var rssi = RSSI as! Int
        if name.contains(nameDevice) && rssi > -65 {
                // Kết nối đến thiết bị BLE
            print("ket noi thanh cong")
            labelStatus.text = "접속완료"
            centralManager.connect(peripheral, options: nil)
        } else {
        }

    }
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        // Thực hiện các thao tác cần thiết sau khi kết nối thành công
        peripheral.discoverServices(nil)

    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if error == nil {
            for service in peripheral.services ?? [] {
                peripheral.discoverCharacteristics(nil, for: service)
            }
        } else {
            print("Lỗi khi tìm kiếm các service: \(error!.localizedDescription)")
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if error == nil {
            for characteristic in service.characteristics ?? [] {
                // Lưu các characteristic vào biến để sử dụng sau này
                self.myCharacteristic = characteristic
            }
        } else {
            print("Lỗi khi tìm kiếm các characteristic: \(error!.localizedDescription)")
        }
    }

    
    // Button
    @IBAction func getOpenKey(_ sender: Any) {

        var t = self.linkLocal ?? ""
        var p = self.roomNumber ?? ""
        print(t)
        self.getRequest(t + "/v1/guest/local/ble-access-key")
        self.stringNameDevice = "EstBM(" + p + ")"
        self.sendAct()
        self.sendData(self.keyaccess)
    }
    @IBAction func getCallKey(_ sender: Any) {
        var t = self.linkLocal ?? ""
        self.getRequest(t + "/v1/guest/local/elevator-access-key")
        self.stringNameDevice = "EstEV"
        self.sendAct()
        self.sendData(self.keyaccess)
    }
    
    func getRequest(_ link: String){
        let m1 = ViewController()
        var s = self.userName ?? ""
        let getString = "?phoneNumber=" + s
        let url = URL(string: link + getString)
        guard let requestUrl = url else { fatalError() }
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "GET"
        request.addValue(m1.apiKey, forHTTPHeaderField: "x-api-key")
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let data = data {
                do {
                    if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                        if let codes = json["code"] as? Int {
                                print("Code: \(codes)")
                            }
                        if let error = json["error"] as? NSNull{
                                print("OKOK: \(error)")
                            }

                        if let data = json["data"] as? [String: Any] {
                            //                            print(data)
                            let key = data["bleAccessKey"] as? String ?? ""
                            self.keyaccess = key
                            print(key)
                        } else {
                            print("loi")
                        }
                    }
                } catch {
                    print("Error: \(error.localizedDescription)")
                }
            }
        }
        task.resume()
    }
    
    func getRoomNumber(_ phoneNumber: String,_ confirmationCode: String) {
        let t = self.linkLocal ?? ""
        let url = URL(string: t + "/v1/guest/local/confirm")
        guard let requestUrl = url else { fatalError() }
        // Prepare URL Request Object
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "POST"
        let api = self.apikey ?? ""
        request.addValue(api, forHTTPHeaderField: "x-api-key")
        // HTTP Request Parameters which will be sent in HTTP Request Body
        let postString = "phoneNumber="+phoneNumber+"&confirmationCode="+confirmationCode;
        // Set HTTP Request Body
        request.httpBody = postString.data(using: String.Encoding.utf8);
        // Perform HTTP Request
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
                
                // Check for Error
                if let error = error {
                    print("Error took place \(error)")
                    return
                }
            if let data = data {
                do {
                    if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                        if let codes = json["code"] as? Int {
                                print("Code: \(codes)")
                            }
                        if let error = json["error"] as? NSNull{
                                print("OKOK: \(error)")
                            }

                        if let data = json["data"] as? [String: Any] {
                            let roomN = data["roomNumber"] as? String ?? ""
                            self.roomNumber = roomN
//                            print(roomN)
                        } else {
                            print("loi")
                        }
                    }
                } catch {
                    print("Error: \(error.localizedDescription)")
                }
            }
        }
        task.resume()
    }
    
    func splitByteArray(_ byteArray: [UInt8], maxLength: Int) -> [[UInt8]] {
        var result: [[UInt8]] = []
        var index = byteArray.startIndex
        while index < byteArray.endIndex {
            let endIndex = byteArray.index(index, offsetBy: maxLength, limitedBy: byteArray.endIndex) ?? byteArray.endIndex
            let chunk = Array(byteArray[index..<endIndex])
            result.append(chunk)
            index = endIndex
        }
        return result
    }
    
    func sendData(_ send: String) {
        if let peripheral = peri {
            if let service = peripheral.services?.first(where: { $0.uuid == CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb") }) {
                // Tìm đặc tính có UUID tương ứng trong dịch vụ
                if let characteristic = service.characteristics?.first(where: { $0.uuid == CBUUID(string: "0000ffe1-0000-1000-8000-00805f9b34fb") }) {
                    if let data = send.data(using: .utf8) {
                        let maxLength = 20 // hoặc 22 nếu sử dụng BLE 4.2
                        var offset = 0
                        while offset < data.count {
                            let length = min(data.count - offset, maxLength)
                            let packet = data.subdata(in: offset..<offset+length)
                            peri?.writeValue(data, for: characteristic, type: .withResponse)
                            offset = offset + length
                            Thread.sleep(forTimeInterval: 0.1) // đợi 100ms trước khi gửi gói dữ liệu tiếp theo
                        }
                    }
                } else {
                    print("Không tìm thấy characteristic để gửi dữ liệu")
                }
            } else {
                print("Chưa kết nối với thiết bị BLE")
            }
        }
    }
    
    func sendAct() {
        let data = "Act:5282"
        if let peripheral = peri {
            if let service = peripheral.services?.first(where: { $0.uuid == CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb") }) {
                // Tìm đặc tính có UUID tương ứng trong dịch vụ
                if let characteristic = service.characteristics?.first(where: { $0.uuid == CBUUID(string: "0000ffe1-0000-1000-8000-00805f9b34fb") }) {
                    if let data = data.data(using: .utf8) {
                        let maxLength = 20 // hoặc 22 nếu sử dụng BLE 4.2
                        var offset = 0
                        while offset < data.count {
                            let length = min(data.count - offset, maxLength)
                            let packet = data.subdata(in: offset..<offset+length)
                            peri?.writeValue(data, for: characteristic, type: .withResponse)
                            offset = offset + length
                            Thread.sleep(forTimeInterval: 0.1) // đợi 100ms trước khi gửi gói dữ liệu tiếp theo
                        }
                    }
                } else {
                    print("Không tìm thấy characteristic để gửi dữ liệu")
                }
            } else {
                print("Chưa kết nối với thiết bị BLE")
            }
        }
    }
}
