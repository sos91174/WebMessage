package test;

import java.io.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.*;
import org.w3c.dom.*;



@WebServlet("/MessageBoardServlet")
public class MessageBoardServlet extends HttpServlet {

    private List<Message> messages = null;
    private File messagesFile = null;
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        // 用戶名
        String user = null;

        // 存放message的xml文件
        messagesFile = new File(this.getServletContext().getRealPath(""), "messages.xml");

        // 設置文字編碼
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 設定幾秒刷新
        response.setHeader("refresh", "30");

        // 判斷是否為新用戶，如果是則創建 new ArrayList<Message>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user".equals(cookie.getName())) {
                    user = cookie.getValue();
                }
            }
        } 

        try {
            // 讀取文件
            messages = list(messagesFile);

        } catch (Exception e) {
            System.out.println("文件錯誤：list()");
            e.printStackTrace();
        }

        // 獲取所有參數
        String username = request.getParameter("username");
        String messageContent = request.getParameter("messageContent");

        Message message = null;

        if (username != null && !"".equals(username)
                && messageContent != null && !"".equals(messageContent)) {

            // 設置用戶名字的cookie
//            Cookie userCookie = new Cookie("user", URLEncoder.encode(username, "UTF-8"));
            Cookie userCookie = new Cookie("user", null);
            userCookie.setPath("/");
            userCookie.setMaxAge(-1);
            response.addCookie(userCookie);

            message = new Message();
            message.setIp(request.getRemoteAddr());
            message.setUsername(username);
            message.setContent(messageContent);
            message.setDate(new Date());

            try {
                addMessageToFile(messagesFile, message);
            } catch (Exception e) {
                System.out.println("xml文件錯誤: addMessageToFile()");
                e.printStackTrace();
            }

            messages.add(message);

            // 更新用戶名
            user = username;
        }

        // 輸出
        PrintWriter out = response.getWriter();

        // 呈現頁面訊息
        out.println("<b>[留言内容], [IP], [用戶名], [時間]</b>");
        out.println("<hr>");

        // 日期格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (messages != null) {
            for (Message m : messages) {

                out.println("[" + m.getContent() + "], [" + m.getIp() + "], [" + m.getUsername() + "], [" + sdf.format(m.getDate()) + "]");
                out.println("<hr>");
            }
        }

        out.println("");
        out.println("<form  method='post'>");
        out.println("用戶名： <input type='text' name='username' value='" + (user == null ? "" : URLDecoder.decode(user, "UTF-8")) + "' /><br/>");
        out.println("留言内容：<textarea name='messageContent' cols='30' rows='5'></textarea><br>");
        out.println("<input type='submit' value='送出' />");
        out.println("<input type='submit' value='登出' id='button1' onclick=javascript:location.href='http://localhost:8080/0806_2/cartEnd' />");

    }

    // 向xml文件添加内容
    private void addMessageToFile(File file, Message message) throws Exception {

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        // 創造message元素
        Element messageElement = document.createElement("message");
        // 創造content元素
        Element contentElement = document.createElement("content");
        // 創造ip元素
        Element ipElement = document.createElement("ip");
        // 創造username元素
        Element usernameElement = document.createElement("username");
        // 創造date元素
        Element dateElement = document.createElement("date");

        // 獲取元素
        Element rootElement = document.getDocumentElement();

        // 把messageElement添加到根元素下
        rootElement.appendChild(messageElement);
        // 把content, ip, username元素添加到message元素下
        messageElement.appendChild(contentElement);
        messageElement.appendChild(ipElement);
        messageElement.appendChild(usernameElement);
        messageElement.appendChild(dateElement);

        // 設置
        contentElement.setTextContent(message.getContent());
        ipElement.setTextContent(message.getIp());
        usernameElement.setTextContent(message.getUsername());
        dateElement.setTextContent(message.getDate().getTime() + "");

        // 將內存數據載到文件
        Source xmlSource = new DOMSource(document);
        Result outputTarget = new StreamResult(file);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(xmlSource, outputTarget);

    }

    // 獲取xml文件中的message
    private List<Message> list(File file) throws Exception {
        List<Message> list = new ArrayList<Message>();

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        NodeList MessageNodes = document.getElementsByTagName("message");

        for (int i = 0; i < MessageNodes.getLength(); i++) {
            Element messageElement = (Element) MessageNodes.item(i);

            Message message = new Message();

            message.setContent(messageElement.getElementsByTagName("content").item(0).getTextContent());
            message.setIp(messageElement.getElementsByTagName("ip").item(0).getTextContent());
            message.setUsername(messageElement.getElementsByTagName("username").item(0).getTextContent());
            message.setDate(new Date(Long.parseLong(messageElement.getElementsByTagName("date").item(0).getTextContent())));

            list.add(message);
        }

        return list;

    }

    // 清空xml文件内容
    private void clearXmlFile(File file) throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        // 節點
        Element rootElement = document.getDocumentElement();

        NodeList messageList = document.getElementsByTagName("message");

        // 刪除所有message節點
        while (messageList.getLength() != 0) {
            rootElement.removeChild(messageList.item(0));
        }

        // 把內存數據放到文件
        Source xmlSource = new DOMSource(document);
        Result outputTarget = new StreamResult(file);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(xmlSource, outputTarget);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 獲取GET傳遞引數名稱
        String delName = request.getParameter("name");
        if (delName == null) {
            response.sendRedirect("./CookieRead");
            return;
        }

        // 獲取Cookies陣列
        Cookie[] cookies = request.getCookies();
        // 迭代查詢並清除Cookie
        for (Cookie cookie: cookies) {
            if (delName.equals(cookie.getName())) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        response.setHeader("refresh", "3;./CookieRead");
        PrintWriter out = response.getWriter();
        out.println("Will go back after 3 seconds...");
    }
    

    
    

}
