window.onload = function(){
	console.log("执行js");
	var ws = new WebSocket("ws://localhost:8080/ws/websocket/admin");
		ws.onopen = function()
		{  
			// 发送消息
			console.log('open');
			ws.send('hello');
		};
		ws.onmessage = function(evt)
		{
		  if(evt.data != "isOK"){
			  var content = document.getElementById('content');
			  var child = document.getElementById('tip');
			  content.removeChild(child);
			  
			  var p = document.createElement("p");
			  var a = document.createElement("a");
			  var text = document.createTextNode("你有一个文件待");
			  p.appendChild(text);
			  text = document.createTextNode("下载");
			  a.appendChild(text);
			  a.classList.add("navbar-link");
			  a.setAttribute("href",evt.data); 
			  p.appendChild(a);
			  p.classList.add('navbar-text');
			  content.appendChild(p);
		  }
			// 监听消息
		  console.log(evt.data);
		  
		  
		  /*<p class="navbar-text">你有一个文件待<a href="#" class="navbar-link">下载</a></p>*/
		};
		ws.onclose = function(evt)
		{
		  // 关闭连接
		  console.log('WebSocketClosed!');
		};
		ws.onerror = function(evt)
		{
		  // 发生错误
		  console.log('WebSocketError!');
		};
}


