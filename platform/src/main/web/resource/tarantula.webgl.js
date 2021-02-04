//PIXI JS WRAPPER
var TGL = (function(){
    var _tpx_app;
    var _tpx_start = function(cview){
        PIXI.utils.skipHello();
        let rps ={resizeTo: window,view: cview,antialiasing: true,backgroundColor : 0x006600};
        _tpx_app = new PIXI.Application(rps);
        return _tpx_app;
    };
    var _tpx_shutdown = function(){
        _tpx_app.stage.removeChildren();
        PIXI.utils.clearTextureCache();
        PIXI.utils.destroyTextureCache();
        _tpx_app.destroy(true);
    };
    var _tpx_container = function(){
        return {container:new PIXI.Container()};
    };

    return{
        start  : _tpx_start,
        shutdown : _tpx_shutdown,
        container : _tpx_container,
    };
})();