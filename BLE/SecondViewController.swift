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
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.getRoomNumber(self.userName ?? "", self.password ?? "")
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            central.scanForPeripherals(withServices: nil, options: nil)
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

        self.getRequest(t + "/v1/guest/local/ble-access-key")
        self.stringNameDevice = "EstBM(" + p + ")"
        self.sendData(self.keyaccess)
    }
    @IBAction func getCallKey(_ sender: Any) {
        var t = self.linkLocal ?? ""
        self.getRequest(t + "/v1/guest/local/elevator-access-key")
        self.stringNameDevice = "EstEV"
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
    func sendData(_ send: String) {
        if let characteristic = self.myCharacteristic {
            let data = send.data(using: .utf8)!
            self.peri?.writeValue(data, for: characteristic, type: .withResponse)
        } else {
            print("Không tìm thấy characteristic để gửi dữ liệu")
        }
    }
}
