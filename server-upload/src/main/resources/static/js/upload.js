/*window.onload = function(){
	console.log("js开始执行。");
	var uploader = WebUploader.create({

	    // swf文件路径
	    swf: '/js/Uploader.swf',

	    // 文件接收服务端。
	    server: 'http://localhost:8080/upload/chunk',

	    // 选择文件的按钮。可选。
	    // 内部根据当前运行是创建，可能是input元素，也可能是flash.
	    pick: '#picker',
	    auto: false,
	    threads:3,
        chunked: true,  //分片处理
        chunkSize: 1024, //每片5M  
        //threads:1,//上传并发数。允许同时最大上传进程数。
	    // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
	    resize: false
	});
	
	// 当有文件被添加进队列的时候
	uploader.on( 'fileQueued', function( file ) {
	    $list.append( '<div id="' + file.id + '" class="item">' +
	        '<h4 class="info">' + file.name + '</h4>' +
	        '<p class="state">等待上传...</p>' +
	    '</div>' );
	});
	
	// 文件上传过程中创建进度条实时显示。
	uploader.on( 'uploadProgress', function( file, percentage ) {
	    var $li = $( '#'+file.id ),
	        $percent = $li.find('.progress .progress-bar');

	    // 避免重复创建
	    if ( !$percent.length ) {
	        $percent = $('<div class="progress progress-striped active">' +
	          '<div class="progress-bar" role="progressbar" style="width: 0%">' +
	          '</div>' +
	        '</div>').appendTo( $li ).find('.progress-bar');
	    }

	    $li.find('p.state').text('上传中');

	    $percent.css( 'width', percentage * 100 + '%' );
	});
	
	uploader.on( 'uploadSuccess', function( file ) {
	    $( '#'+file.id ).find('p.state').text('已上传');
	});

	uploader.on( 'uploadError', function( file ) {
	    $( '#'+file.id ).find('p.state').text('上传出错');
	});

	uploader.on( 'uploadComplete', function( file ) {
	    $( '#'+file.id ).find('.progress').fadeOut();
	});
	
}*/

$(function(){

    var $list = $("#thelist");
    var uploader;
　　 // 初始化uploader
    uploader = WebUploader.create({
        auto:false, //不自动提交，需要点击
        pick: {
            id: '#attach',
            label: '选择文件',
        },
        server: 'http://localhost:8080/upload/chunk', //后台接收服务
        resize: false,
        formData: {"Authorization": localStorage.token}, //额外参数传递，可以没有
        chunked: true, //分片
        chunkSize: 1024, //分片大小指定
        threads:1, //线程数量
        disableGlobalDnd: true //禁用拖拽
    });

    //添加文件页面提示
    uploader.on( "fileQueued", function( file ) {
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
        $("#"+file.id ).find("p.state").text("已上传");
        $("#"+file.id).find(".progress").fadeOut("normal");
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
    })

});


