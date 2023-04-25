//
//  ViewController.swift
//  BLE
//
//  Created by Anh Đức on 22/04/2023.
//

import UIKit
import Parse

struct Data {}
class ViewController: UIViewController, UITextFieldDelegate {
    let apiKey = "3cc5abbc5e2e497db70731661d96d812"
    var st = "" // link local get after call api
    var username = ""
    var password = ""
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
        textUsername.delegate = self
        textPassword.delegate = self
    }
    
    @IBAction func signin(_ sender: UIButton) {
//        self.sendConfirmationRequest()
        if isSigningIn {
            DispatchQueue.main.async {
                self.performSegue(withIdentifier: "abc", sender: nil)
            }
        } else {
            print("not ok")
        }
      
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        // Xử lý sự kiện thay đổi của text field ở đây
//        print("Text field changed: \(textUsername.text ?? "")")
        self.username = self.textUsername.text ?? ""
        self.password = self.textPassword.text ?? ""
        self.sendConfirmationRequest()
        return true
    }
    
    func displayAlert(withTitle title: String, message: String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: "Ok", style: .default)
        alert.addAction(okAction)
        self.present(alert, animated: true)
    }
    

    func sendConfirmationRequest() {
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
                            self.st = (data["hotelUrl"] as? String ?? "")
                            self.isSigningIn = true
                        } else {
                            self.isSigningIn = false
                        }
                    }
                } catch {
                    print("Error: \(error.localizedDescription)")
                }
            }
        }
        task.resume()

    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let destinationViewController = segue.destination as? SecondViewController {
            destinationViewController.linkLocal = self.st
            destinationViewController.userName = self.username
            destinationViewController.password = self.password
            destinationViewController.apikey = self.apiKey
        }
    }
}

