window.$api = {}

window.$name = function (n) {
    return $(document.getElementsByName(n)[0])
};

Date.prototype.$format = function (fmt) { //author: meizz
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

window.$api.getBlockHashByHeight = function (height, cb) {
    if (cb) {
        $api.post('/api/proxy', {
            path: '/rpc/block/' + height
        }, (data, err) => {
            cb(data && data.hash, err)
        })
        return
    }
    return $api.post('/api/proxy', {
        path: '/rpc/block/' + height
    }).then(d => d.hash)
}


window.$api.getSender = function (cb) {
    return $api.get('/api/express/sender', cb)
}

window.$api.getOrder = function (cb) {
    return $api.get('/api/express/order', cb)
}

window.$api.get = function (url, cb) {
    return $api.request(url, '', cb, 'get')
}

window.$api.request = function (url, data, cb, method) {
    if (cb) {
        $.ajax({
            url: url,
            type: method,
            data: typeof data === 'string' ? data : JSON.stringify(data),
            contentType: 'application/json',
            success: (data) => {
                if (typeof data == 'string')
                    data = JSON.parse(data)
                if (data && data.data && data.code < 400) {
                    cb(data.data)
                } else {
                    cb(undefined, (data && data.message || 'error'))
                }
            }
        })
        return
    }
    return new Promise((resolve, reject) => {
        $.ajax({
            url: url,
            type: method,
            data: typeof data === 'string' ? data : JSON.stringify(data),
            contentType: 'application/json',
            success: (data) => {
                if (typeof data == 'string')
                    data = JSON.parse(data)
                if (data && data.data && data.code < 400) {
                    resolve(data.data)
                    return
                }
                reject(new Error(data && data.message || 'error'))
            }
        })
    })
}

window.$api.post = function (url, data, cb) {
    return $api.request(url, data, cb, 'post')
}


window.$api.patch = function (url, data, cb) {
    return $api.request(url, data, cb, 'patch')
}

window.$getUrlParameter = function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
};
