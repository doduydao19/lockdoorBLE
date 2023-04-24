//
//  RequestAPI.swift
//  BLE
//
//  Created by Anh Đức on 23/04/2023.
//

import Foundation

let apiKey = "3cc5abbc5e2e497db70731661d96d812"
let username = "01030719423"
let password = "111111"

func sendConfirmationRequest() {
    guard let url = URL(string: "https://cloud.estech777.com/v1/auth/confirm") else {
        return
    }
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    //request.addValue("application/json", forHTTPHeaderField: "Content-Type")
    request.addValue(apiKey, forHTTPHeaderField: "x-api-key")

    let parameters = ["username": username, "password": password]
    request.httpBody = try? JSONSerialization.data(withJSONObject: parameters, options: [])

    let session = URLSession.shared
    let task = session.dataTask(with: request) { data, response, error in
        guard let data = data, error == nil else {
            print(error?.localizedDescription ?? "No data")
            return
        }

        let responseJSON = try? JSONSerialization.jsonObject(with: data, options: [])
        if let responseJSON = responseJSON as? [String: Any] {
            // handle response data
        }
    }
    task.resume()
}

