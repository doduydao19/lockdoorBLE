//
//  ViewController.swift
//  BLE
//
//  Created by Anh Đức on 22/04/2023.
//

import UIKit
import Parse

struct Data {}
class ViewController: UIViewController {
    let apiKey = "3cc5abbc5e2e497db70731661d96d812"
    let data = Data()
    var st = ""
    @Published var hasError = false

    @Published var isSigningIn = false
    let semaphore = DispatchSemaphore(value: 0)
    @IBOutlet weak var textUsername: UITextField!
    @IBOutlet weak var textPassword: UITextField!
    
    @IBOutlet weak var indicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
//        testParseConnection()
        let backgroundImage = UIImageView(frame: UIScreen.main.bounds)
        backgroundImage.image = UIImage(named: "bg_guest_start")
        backgroundImage.contentMode = .scaleAspectFill
        view.insertSubview(backgroundImage, at: 0)

    }
    
    @IBAction func signin(_ sender: UIButton) {
      sendConfirmationRequest() { isSigningIn in
        if isSigningIn {
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "abc", sender: nil)
            }
        } else {
            print("not ok")
        }
    }
    }
    func displayAlert(withTitle title: String, message: String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: "Ok", style: .default)
        alert.addAction(okAction)
        self.present(alert, animated: true)
    }
    

    func sendConfirmationRequest(completion: @escaping (Bool) -> Void) {
//        self.hasError = true
//        let url = URL(string: "https://cloud.estech777.com/v1/auth/confirm")!
////        let authData = (self.username + "=" + self.password).data(using: .utf8)!.base64EncodedString()
//        var request = URLRequest(url: url)
//
//        request.addValue(self.apiKey, forHTTPHeaderField: "x-api-key")
//        request.httpMethod = "POST"
        let phoneNumber = self.textUsername.text ?? ""
        let confirmationCode = self.textPassword.text ?? ""
        self.st = phoneNumber

//
        let url = URL(string: "https://cloud.estech777.com/v1/auth/confirm")
        guard let requestUrl = url else { fatalError() }
        // Prepare URL Request Object
        var request = URLRequest(url: requestUrl)
        request.httpMethod = "POST"
        request.addValue(self.apiKey, forHTTPHeaderField: "x-api-key")
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
         
                // Convert HTTP Response Data to a String
//                if let data = data, let dataString = String(data: data, encoding: .utf8) {
//                    print("Response data string:\n \(dataString)")
//                }
            if let data = data {
                do {
                    if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                        if let codes = json["code"] as? Int {
                                print("Code: \(codes)")
                            }
                        print(json)
                        if let error = json["error"] as? NSNull{
                                print("OKOK: \(error)")
                            }
//                        let errors = json["error"] as? String ?? ""
                        if let data = json["data"] as? [String: Any] {
                                print("Hotel URL: \(data)")
                            self.isSigningIn = true
                        } else {
                            self.isSigningIn = false
                        }
//                        if datat != ""{
//                            self.isSigningIn = false
//                        } else {
//                            self.isSigningIn = true
//                        }
                    }
                } catch {
                    print("Error: \(error.localizedDescription)")
                }
            }
        }
        task.resume()
        completion(isSigningIn)
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let destinationViewController = segue.destination as? SecondViewController {
            destinationViewController.data = self.st
        }
    }
}

