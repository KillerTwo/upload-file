$(function(){
	console.log("执行上传文件。。。");
	var size = 0;	//文件大小
	var fileMd5 = '';  //	文件的MD5值
    var $list = $("#thelist");
    var uploader;
　　 // 初始化uploader
    uploader = WebUploader.create({
        auto:false, 					//不自动提交，需要点击
        pick: {
            id: '#attach',
            label: '选择文件',
        },
        server: '/chunk', //后台接收服务
        resize: false,
        formData: {
        	"Authorization": localStorage.token,
        	"isFinish": 1,
        	//"fileMd5": fileMd5
        }, 							//额外参数传递，可以没有
        chunked: true, 									//分片
        chunkSize: 1024 * 1024 * 30, 					//分片大小指定
        threads:1, 										//线程数量
        disableGlobalDnd: true,						//禁用拖拽
        compress: false,							// 不要缩图片文件
        chunkRetry: 3	,							// 重传次数
        //fileNumLimit: 1								// 文件总数量（一次只能上传一个）
        method: 'POST',
        fileVal: 'file'								// 文件域的name
    });

    //添加文件页面提示
    uploader.on( "fileQueued", function( file ) {
    	size = file.size;
    	console.log(file.size);
    	uploader.md5File( file )
        .then(function(val) {
        	file.wholeMd5 = val; //设置md5值
        	// 修改uploader的formData选项
        	uploader.option( 'formData', {
        	    'fileMD5': val,
        	    'ext': file.ext
        	});
            console.log('md5 result:', val);
        });
        $list.html( "<div id='"+  file.id + "' class='item'>" +
            "<h4 class='info'>" + file.name + "</h4>" +
            "<p class='state'>等待上传...</p>" +
            "</div>" );
    });
    //开进度条
    uploader.on( 'uploadProgress', function( file, percentage ) {
        var $li = $( '#'+file.id ),
            $percent = $li.find('.progress .progress-bar');
        if ( !$percent.length ) {
            $percent = $('<div class="progress progress-striped active">' +
                '<div class="progress-bar" role="progressbar" style="width: 0%">' +
                '</div>' +
                '</div>').appendTo( $li ).find('.progress-bar');
        }
        $li.find('p.state').text('上传中');
        $percent.css( 'width', percentage * 100 + '%' );
    });
    //上传成功
    uploader.on( "uploadSuccess", function( file ) {
        
        //$("#"+file.id).find(".progress").fadeOut("normal");
       /* axios.post('http://localhost:8080/upload/chunk',{
        	"fileMD5": file.wholeMd5, 
        	"isFinish": 0
        	})
        	.then(function(res){
        	  console.log("上传成功：", res);
        	})
        	.catch(function(err){
        	  console.log(err);
        	});*/
    	// 前端将所有的包已经发送完成则重新发送一个整个文件发送完成的请求给服务器，待服务器处理完成后返回文件是否正确被接收方接收的标志
        axios.get('/chunk',{
        	  params:{
        		"fileMD5": file.wholeMd5, 
              	"isFinish": 0,
              	"fileName": file.name,
              	"ext": file.ext
        	  }
        	})
        	.then(function(response){
        		if(response.data == 0){
        			$("#"+file.id ).find("p.state").text("已上传");				// 将提示该为已上传
            		//$("#"+file.id).find(".progress").fadeOut("normal");			// 上传成功，删除进度条。
            		$("#"+file.id).find(".progress").hide();
            		console.log("上传成功：", response);
        		}else{
        			$("#"+file.id).find("p.state").text("上传失败");				// 将提示改为上传失败
            		$("#"+file.id).find(".progress").hide();
        		}
        	})
        	.catch(function(err){
        		console.log("上传失败。。。",err);
        		location.reload();   
        	});
        ////
    });

    //上传失败
    uploader.on( "uploadError", function( file ) {
        $( "#"+file.id ).find("p.state").text("上传出错");
        uploader.cancelFile(file);
        uploader.removeFile(file,true);
    });
    //点击上传
    $("#upload").on("click", function() {
        uploader.upload();
    });

});


