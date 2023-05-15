package com.example.myapplication.data;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.example.myapplication.data.model.LoggedInUser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    protected Boolean r = false;
    protected String roomNumber = "";
    protected String linkLocal;

    public String keyAccessBLE;


    public Result<LoggedInUser> login(String username, String password) {
        try {
            class PostRequestApi extends AsyncTask<Void, Void, Boolean> {
                private LoginDataSource t;
                public PostRequestApi(LoginDataSource t) {
                    this.t = t;
                }
                @SuppressLint("WrongThread")
                @Override
                protected Boolean doInBackground(Void... voids) {
                    System.out.println(username);
                    return t.sendPost(username, password);
                }
                @Override
                protected void onPostExecute(Boolean result) {
                    t.r = result;
                }
            }
            LoginDataSource t = new LoginDataSource();
            PostRequestApi postRequestApi = new PostRequestApi(t);
            postRequestApi.execute();
            postRequestApi.get();
            this.r = t.r;
//        System.out.println(l[1].split(":").substring(l[1]));
            // TODO: handle loggedInUser authentication
            System.out.println("result" + String.valueOf(t.r));
            if(t.r) {
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                "12344",
                                "Tester", t.roomNumber, t.linkLocal);
                return new Result.Success<>(fakeUser);
            }
            else {
                return new Result.Error(new IOException("Error logging in"));
            }
        } catch (Exception e) {
//            System.out.println("dasdasdasdasdasdadafa");
            return new Result.Error(new IOException("Error logging in", e));
        }
    }
    public Boolean sendPost(String username, String password) {
        try {
            // Thay thế URL của API cần gọi
            URL url = new URL("https://cloud.estech777.com/v1/auth/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Thiết lập phương thức POST
            connection.setRequestMethod("POST");

            // Thiết lập header để cung cấp API key
            connection.setRequestProperty("x-api-key", "3cc5abbc5e2e497db70731661d96d812");

            // Thiết lập nội dung của yêu cầu
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(("phoneNumber=" + username + "&confirmationCode=" + password).getBytes());
            outputStream.flush();
            outputStream.close();

            // Đọc phản hồi từ API
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(",");
                String check = s[1].substring("\"data\":".length(), s[1].length());
                if(!check.equals("null")) {
                    String[] listLink = check.replace("\"", "").split(":");
                    String linkLocal = listLink[1]+":" + listLink[2].substring(0, listLink[2].length()-1);
                    this.linkLocal = linkLocal;
                    sendPostR(username, password, linkLocal);
                    this.r =  true;
                    return true;
                } else {
                    this.r = false;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public Boolean sendPostR(String username, String password, String link) {
        try {
            // Thay thế URL của API cần gọi
            String urll = link + "/v1/guest/local/confirm";
            URL url = new URL(urll);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Thiết lập phương thức POST
            connection.setRequestMethod("POST");

            // Thiết lập header để cung cấp API key
            connection.setRequestProperty("x-api-key", "3cc5abbc5e2e497db70731661d96d812");

            // Thiết lập nội dung của yêu cầu
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(("phoneNumber=" + username + "&confirmationCode=" + password).getBytes());
            outputStream.flush();
            outputStream.close();

            // Đọc phản hồi từ API
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(",");
                String data = s[1].substring("\"data\":{".length(), s[1].length()-1);
                String roomNumber = s[2].split(":")[1].replace("\"", "");

                this.roomNumber = roomNumber;
                if(!data.equals("null")) {
                    this.r =  true;
                    return true;
                } else {
                    this.r = false;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void sendGet(String urll, String phoneNumber) {
        try {
            String url = urll + "?phoneNumber=" + phoneNumber;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setRequestProperty("x-api-key", "3cc5abbc5e2e497db70731661d96d812");
            //add request header
//        con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                String s = String.valueOf(response);
                String[] dta = s.split(",");
                String preKey = dta[1].substring("\"data\":{".length(), dta[1].length() -1);
                keyAccessBLE = preKey.split("\"")[3];

            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}