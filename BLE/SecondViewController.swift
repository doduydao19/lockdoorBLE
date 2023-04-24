//
//  SecondViewController.swift
//  BLE
//
//  Created by Anh Đức on 22/04/2023.
//

import UIKit

class SecondViewController: UIViewController {
    var data: String?
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

    @IBAction func getOpenKey(_ sender: Any) {
        self.getRequest("https://cloud.estech777.com/v1/guest/local/ble-access-key")
    }
    @IBAction func getCallKey(_ sender: Any) {
        self.getRequest("https://cloud.estech777.com/v1/guest/local/elevator-access-key")
    }
    
    func getRequest(_ link: String){
        let m1 = ViewController()
        var s = self.data as? String ?? ""
        let getString = "?phoneNumber=" + s
        let url = URL(string: link + getString)
        guard let requestUrl = url else { fatalError() }
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "GET"
        request.addValue(m1.apiKey, forHTTPHeaderField: "x-api-key")
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let error = error {
                    print("Error took place \(error)")
                    return
                }
                
                // Read HTTP Response Status code
                if let response = response as? HTTPURLResponse {
                    print("Response HTTP Status code: \(response.statusCode)")
                }
                
                // Convert HTTP Response Data to a simple String
                if let data = data, let dataString = String(data: data, encoding: .utf8) {
                    print("Response data string:\n \(dataString)")
                }
        }
        task.resume()
    }
}
