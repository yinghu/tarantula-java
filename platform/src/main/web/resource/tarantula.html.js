var TARA_HTML = (function(){
    let _caption = function(word){
        return word[0].toUpperCase() + word.slice(1).toLowerCase()
    }
    let _command = function(itemId,resource,name,className,actionName){
        let tem = [];
        tem.push('<div class=\'w3-container w3-border-bottom w3-border-red\'>');
        tem.push('<span class=\'w3-left tx-text-14 tx-margin-top-8\'><i class=\'fa fa-hourglass-1\' style=\'font-size:24px;color:blue\'></i></span>');
        tem.push('<span class=\'w3-left tx-text-14 tx-margin-top-8 tx-margin-left-16\'>');
        tem.push(resource);
        tem.push('</span><span class=\'w3-left tx-text-14 tx-margin-top-8 tx-margin-left-12\'>');
        tem.push(name);
        tem.push('</span><span tx-item-id=\''+itemId+'\'');
        tem.push(' class=\'w3-right w3-tag w3-green w3-round w3-border w3-border-red tx-text-16 tx-padding-button tx-margin-top-8 tx-margin-bottom-8');
        tem.push(' tx-live-'+className+'\'');
        tem.push('>'+actionName+'</span>');
        tem.push('</div>'); 
        return tem.join('');  
    };
    let _application = function(app,className){
        let tem = [];
        tem.push('<span tx-service-tag=\'');
        tem.push(app.tag);
        tem.push('\' view-id=\'');
        tem.push('live.game.service.'+app.name.toLowerCase());
        tem.push('\' tx-service-category=\''+app.category);
        tem.push('\' class=\'w3-bar-item w3-tag w3-right w3-round w3-border w3-border-red w3-green tx-text-12 tx-padding-button tx-margin-bottom-8 tx-margin-left-4');
        tem.push(' tx-game-'+className+'\'>');// tx-game-service-bar\'>');
        tem.push(app.name);
        tem.push('</span>');
        return tem.join('');
    }
    let _option = function(value,view,selected){
        let tem =[];
        if(selected){
            tem.push('<option value=\''+value+'\' selected>'+view+'</option>');
        }
        else{
            tem.push('<option value=\''+value+'\'>'+view+'</option>');
        }
        return tem.join('');
    };  
    let _bar_item = function(id,caption,pcss){
        let tem =[];
        tem.push('<span class=\'w3-bar-item ');
        tem.push(pcss)
        tem.push(' w3-tag w3-round w3-border w3-border-red w3-green tx-text-12 tx-padding-button tx-margin-bottom-8 tx-margin-left-4\'');
        tem.push(' id=\'');
        tem.push(id);
        tem.push('\'>');
        tem.push(caption);
        tem.push('</span>');
        return tem.join('');
    };   
    return{
        command : _command,
        option : _option,
        caption : _caption,
        application : _application,
        BarItem: _bar_item
    };
})();