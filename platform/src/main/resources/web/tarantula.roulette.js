var ROULETTE = (function(){
    var _oswaldFamily = 'Oswald, Arial';

    var _tpx_base_v5 = function(cfg){
        const ret={};
        ret.command=function(){};
        const pl = new PIXI.Graphics();
        let b=4;
        let r=16;
        let w=120;
        let h=100;
        if(cfg.h){h=cfg.h;}
        if(cfg.w){w=cfg.w;}
        if(cfg.r){r=cfg.r;}
        if(cfg.b){b=cfg.b;}
        pl.lineStyle(b,0xffffff,1);
        pl.beginFill(0xffffff,1);
        pl.drawRoundedRect(0,0,w-b,h-b,r);
        pl.endFill();
        if(cfg.t){pl.tint = cfg.t;}else{pl.tint=0x0066ff;}
        if(cfg.a){pl.alpha=cfg.a;}
        pl.interactive=false;
        pl.buttonMode=false;
        pl.cursor='';
        ret.up=function(){pl.tint=0x33ff99;};
        ret.out=function(){pl.tint=0x0066ff;};
        ret.over=function(){pl.tint=0x33ff99;};
        ret.move=function(){};
        pl.on('pointerup',function(){ret.up();});
        pl.on('pointerout',function(){ret.out();});
        pl.on('pointerupoutside',function(){ret.up();});
        pl.on('pointerover',function(){ret.over();});
        pl.on('pointermove',function(){ret.move();});
        pl.on('pointerdown',function(e){ret.command();});
        pl.on('added',function(){});
        ret.base=pl;
        ret.set=function(mode){pl.cursor=mode.cursor;pl.interactive=mode.interactive;pl.buttonMode=mode.interactive;};
        ret.width=w;
        ret.height=h;
        ret.round = r;
        ret.border = b;
        return ret;
     };
     var _tpx_icon_v5 = function(w,h){
         const ic = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic.width = w;
         ic.height = h;
         return ic;
     };
     var _tpx_mask_v5 =function(cfg){
        const p = new PIXI.Graphics();
        p.beginFill(0x000000,1);
        p.lineStyle(0,0x000000,1);
        p.drawCircle(0,0,cfg.r);
        p.endFill();
        p.width = cfg.r*2;
        p.height = cfg.r*2;
        return p;
     };
    var _tpx_rect = function(w,h,c){
        const p = new PIXI.Graphics();
        p.command = function(){};
        p.beginFill(c,1);
        p.lineStyle(4,0xffffff,1);
        p.drawRect(0,0,w-4,h-4);
        p.endFill();
        p.interactive = true;
        p.buttonMode = true;
        p.alpha = 1;
        p.on('pointerover',function(){
            p.alpha = 0.5;
        });
        p.on('pointerout',function(){
            p.alpha = 1;
        });
        p.on('pointerdown',function(e){
            p.alpha = 1;
            p.command();
        });
        return p;
    }
    var _tpx_wheel = function(){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 300;
        const h = 240;
        ret.width = w;
        ret.height = w;
        ret.container = ctn;
        const px = new PIXI.Graphics();
        let ed = 2*Math.PI/38;
        let st = ed/2;
        let i,x,y,angle;
        for(i=0;i<38;i++){
            if(i%2===0){
                px.beginFill(0xffffff,1);
            }
            else{
                px.beginFill(0xff0000,1);
            }
            px.lineStyle(2,0x000000,1);
            px.arc(0,0,w,st,ed+st);
            px.lineTo(0,0);
            st = st+ed;
        }
        px.beginFill(0x40ff00,1);
        px.drawCircle(0,0,h-30);
        px.endFill();
        px.pivot.set(-300,-300);
        ctn.addChild(px);
        let t;
        angle = 0;
        ed = Math.PI*2/38;
        let lines =[{t:'0'},{t:'28'},{t:'9'},{t:'26'},{t:'30'},{t:'11'},{t:'7'},{t:'20'},{t:'32'},{t:'17'},{t:'5'},{t:'22'},{t:'34'},{t:'15'},
        {t:'3'},{t:'24'},{t:'36'},{t:'13'},{t:'1'},{t:'00'},{t:'27'},{t:'10'},{t:'25'},{t:'29'},{t:'12'},{t:'8'},{t:'19'},{t:'31'},{t:'18'},
        {t:'6'},{t:'21'},{t:'33'},{t:'16'},{t:'4'},{t:'23'},{t:'35'},{t:'14'},{t:'2'}];
        for(i=0;i<38;i++){
            let p = new PIXI.Graphics();
            p.beginFill(0x40ff00,1);
            p.lineStyle(0,0x000000,1);
            p.drawCircle(0,0,20);
            p.endFill();
            p.x = (w-30)*Math.cos(angle)+w;
            p.y = (w-30)*Math.sin(angle)+w;
            angle +=ed;

            lines[i].x = p.x;
            lines[i].y = p.y;
            t = new PIXI.Text(lines[i].t,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:25,fill:'black'});
            t.x = p.x;
            t.y = p.y;
            t.anchor.set(0.5);
            ctn.addChild(p);
            ctn.addChild(t);
        }
        const py = new PIXI.Graphics();
        py.beginFill(0x000000,1);
        py.lineStyle(0,0x000000,1);
        py.drawCircle(0,0,20);
        py.endFill();
        ctn.addChild(py);
        ret.ball = py;
        ret.lines = lines;
        ret.command = function(){};
        ret.update = function(tm){
            if(ret.stop){
                ret.start = tm;
                ret.stop = false;
                ret.angle = 0;
                ret.lap = 200;
                ret.tx=0;
                ret.ty=0;
            }
            ret.prog = tm -ret.start;
            if(ret.prog<2000){
                ret.container.rotation -= 0.01*1;
                ret.ball.x =(150)*Math.cos(ret.angle)+300;
                ret.ball.y= (150)*Math.sin(ret.angle)+300;
                ret.angle += 0.1;
            }
            else if(ret.prog>=2000){
                ret.container.rotation += 0.02*1;
                if(ret.tx === 0 && ret.ty === 0){
                    ret.tx = (ret.ball.x - ret.lines[ret.stopIndex].x)/ret.lap;
                    ret.ty = (ret.ball.y - ret.lines[ret.stopIndex].y)/ret.lap;
                }
                ret.ball.x = ret.ball.x -(ret.tx);
                ret.ball.y = ret.ball.y -(ret.ty);
                ret.lap--;
                if(ret.lap<0){
                    ret.ball.x = ret.lines[ret.stopIndex].x;
                    ret.ball.y = ret.lines[ret.stopIndex].y;
                    ret.stop = true;
                }
            }
            if(!ret.stop){
                requestAnimationFrame(ret.update);
            }
            else{
                //callback
                ret.command();
            }
        };
        ret.roll = function(callback){
            ret.command = callback;
            requestAnimationFrame(ret.update);
        };
        return ret;
    };
    var _tpx_player = function(w,h){
        const ret={};
        const ctn=new PIXI.Container();
        const frm=_tpx_base_v5({w:w,h:h,r:8,b:4});
        frm.set({cursor:'pointer',interactive:true});
        ctn.addChild(frm.base);
        const ic = _tpx_icon_v5(frm.width,frm.height);
        ic.x = (frm.width-ic.width)/2;
        ic.y = (frm.height-ic.height)/2;
        const msk = _tpx_mask_v5({r:((w/2)-2)});
        msk.x = (frm.width)/2;;
        msk.y = (frm.height)/2;;
        ctn.mask = msk;
        frm.icon = ic;
        ctn.addChild(msk);
        ctn.addChild(ic);
        ret.container = ctn;
        ret.width = frm.width;
        ret.height = frm.height;
        ret.button = frm;
        ret.icon = ic;
        ret.position=function(sc,x,y){ctn.scale.set(sc);ctn.x=x;ctn.y=y;};
        return ret;
    };
    var roulette_number = function(n,c){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 80;
        const h = 120;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const pl = _tpx_rect(w,h,'0x003300');
        const p = new PIXI.Graphics();
        p.beginFill(c,1);
        p.lineStyle(1,0xFFFFFF,1);
        p.drawCircle(0,0,h/4);
        p.endFill();
        p.x = w/2;
        p.y = h/2;
        pl.addChild(p);
        pl.hitArea = new PIXI.Circle(w/2,h/2,h/4);
        //pl.set({cursor:'pointer',interactive:true});
        const nm = new PIXI.Text(n,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:40,fill:'white'});
        nm.anchor.set(0.5);
        nm.rotation = (-1)*Math.PI/2;
        nm.x = (w/2);
        nm.y = (h/2);
        ret.number = nm;
        ctn.addChild(pl);
        ctn.addChild(nm);
        ret.bet=pl;
        return ret;
    };
     var roulette_side_1 = function(n){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 80;
        const h = 120;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const pl = _tpx_rect(w,h,'0x003300');
        const nm = new PIXI.Text(n,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:36,fill:'white'});
        nm.anchor.set(0.5);
        nm.rotation = (-1)*Math.PI/2;
        nm.x = (w/2);
        nm.y = (h/2);
        ret.number = nm;
        ctn.addChild(pl);
        ctn.addChild(nm);
        ret.bet=pl;
        return ret;
    };
    var roulette_side_2 = function(n){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 320;
        const h = 80;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const pl = _tpx_rect(w,h,'0x003300');
        const nm = new PIXI.Text(n,{fontWeight: '700',fontFamily:_oswaldFamily,fontSize:40,fill:'white'});
        nm.anchor.set(0.5);
        nm.x = (w/2);
        nm.y = (h/2);
        ret.number = nm;
        ctn.addChild(pl);
        ctn.addChild(nm);
        ret.bet = pl;
        return ret;
    };
    var roulette_side_3 = function(n){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 160;
        const h = 80;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const pl = _tpx_rect(w,h,'0x003300');
        const nm = new PIXI.Text(n,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:35,fill:'white'});
        nm.anchor.set(0.5);
        nm.x = (w/2);
        nm.y = (h/2);
        ret.number = nm;
        ctn.addChild(pl);
        ctn.addChild(nm);
        ret.bet = pl;
        return ret;
    };
    var roulette_side_4 = function(n,c){
        const ctn = new PIXI.Container();
        const ret ={};
        const w = 160;
        const h = 80;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const pl = _tpx_rect(w,h,c);
        const nm = new PIXI.Text(n,{fontWeight: '700',fontFamily:_oswaldFamily,fontSize:35,fill:'white'});
        nm.anchor.set(0.5);
        nm.x = (w/2);
        nm.y = (h/2);
        ret.number = nm;
        ctn.addChild(pl);
        ctn.addChild(nm);
        ret.bet =pl;
        return ret;
    };

    var _tpx_table = function(){

        let pview = new PIXI.Container();
        let wheel = _tpx_wheel();
        let clist =[0,1,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1];
        let ret ={items:[]};
        ret.items[41] = roulette_side_2('1st 12',);
        let y = ret.items[41].height;
        ret.items[37]= roulette_number('00','0x00ff00',function(){});
        ret.items[37].lineId = '3500';
        ret.items[37].container.x = 0;
        ret.items[37].container.y = ret.items[37].height/2+y;
        pview.addChild(ret.items[37].container);

        ret.items[0]= roulette_number('0','0x00ff00',function(){});
        ret.items[0].lineId = '350';
        ret.items[0].container.x = 0;
        ret.items[0].container.y = ret.items[0].height*1.5+y;
        pview.addChild(ret.items[0].container);
        ret.items[41].lineId = '204';
        ret.items[41].container.x = ret.items[0].width;
        ret.items[41].container.y = 0; //ret.items[0].height*3;
        pview.addChild(ret.items[41].container);

        ret.items[42] = roulette_side_2('2nd 12');
        ret.items[42].lineId = '205';
        ret.items[42].container.x = ret.items[0].width*5;
        ret.items[42].container.y = 0; ///ret.items[0].height*3;
        pview.addChild(ret.items[42].container);

        ret.items[43] = roulette_side_2('3rd 12');
        ret.items[43].lineId = '206';
        ret.items[43].container.x = ret.items[0].width*9;
        ret.items[43].container.y = 0; ///ret.items[0].height*3;
        pview.addChild(ret.items[43].container);

        let rn;
        let x = ret.items[0].width;
        let ix = 1;
        let i,j;
        for(i=0;i<12;i++){
          for(j=0;j<3;j++){
            rn = roulette_number(ix,clist[ix]===1?'0xff00ff':'0x000000');
            rn.lineId = '35'+ix;
            rn.container.x = x;
            rn.container.y = 2*rn.height-j*rn.height+y;
            ret.items[ix++] = rn;
            pview.addChild(rn.container);
          }
          x = x + rn.width;
        }
        ret.items[38] = roulette_side_1('2 to 1');
        ret.items[38].lineId = '201';
        ret.items[38].container.x = x;
        ret.items[38].container.y = y;
        pview.addChild(ret.items[38].container);

        ret.items[39] = roulette_side_1('2 to 1');
        ret.items[39].lineId = '202';
        ret.items[39].container.x = x;
        ret.items[39].container.y = ret.items[39].height+y;
        pview.addChild(ret.items[39].container);

        ret.items[40] = roulette_side_1('2 to 1');
        ret.items[40].lineId = '203';
        ret.items[40].container.x = x;
        ret.items[40].container.y = ret.items[40].height*2+y;
        pview.addChild(ret.items[40].container);

        ret.items[44] = roulette_side_3('1 to 18');
        ret.items[44].lineId = '105';
        ret.items[44].container.x = ret.items[0].width;
        ret.items[44].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[44].container);

        ret.items[45] = roulette_side_3('EVEN');
        ret.items[45].lineId = '102';
        ret.items[45].container.x = ret.items[0].width*3;
        ret.items[45].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[45].container);

        ret.items[46] = roulette_side_4('RED','0xe60000');
        ret.items[46].lineId = '103';
        ret.items[46].container.x = ret.items[0].width*5;
        ret.items[46].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[46].container);

        ret.items[47] = roulette_side_4('BLACK','0x000000');
        ret.items[47].lineId = '104';
        ret.items[47].container.x = ret.items[0].width*7;
        ret.items[47].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[47].container);

        ret.items[48] = roulette_side_3('ODD');
        ret.items[48].lineId = '101';
        ret.items[48].container.x = ret.items[0].width*9;
        ret.items[48].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[48].container);

        ret.items[49] = roulette_side_3('19 to 36');
        ret.items[49].lineId = '106';
        ret.items[49].container.x = ret.items[0].width*11;
        ret.items[49].container.y = ret.items[0].height*3+ret.items[43].height;
        pview.addChild(ret.items[49].container);
        let betList = new PIXI.Container();
        pview.addChild(betList);
        let _target = new PIXI.Sprite(PIXI.Texture.WHITE);
        _target.width = 60;
        _target.height = 60;
        pview.addChild(_target);
        ret.target = _target;
        //pview.addChild(wheel.container);
        //pview.addChild(wheel.container);
        wheel.container.x  = (ret.items[1].width)*14/2;
        wheel.container.y = ((ret.items[1].height)*3+(ret.items[43].height)*2)/2;
        wheel.container.pivot.set(wheel.width,wheel.width);
        wheel.stop = true;
        wheel.container.visible = false;
        pview.addChild(wheel.container);
        ret.betList = betList;
        ret.container = pview;
        ret.wheel = wheel;
        ret.width = ret.items[0].width*14;
        ret.height = ret.items[0].height*3+ret.items[43].height*2;
        pview.pivot.set(ret.width/2,ret.height/2);
        return ret;
    };
    return{
        table : _tpx_table,
        player : _tpx_player,
    };
}());