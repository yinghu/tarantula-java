var BLACKJACK = (function(){
    //var _robotoFamily = "Roboto,Arial";//Arial as backup
    //var _robotoDensFamily = "\'Roboto Condensed\',Arial";//Arial as backup
    var _oswaldFamily = 'Oswald, Arial';
    //var _felipaFamily = 'Felipa, Arial';

     var _tpx_frame = function(w,h,r){
          let ctn = new PIXI.Container();
          let ret ={};
          ret.width = w;
          ret.height = h;
          ret.command = function(){};
          ret.container = ctn;
          let p = new PIXI.Graphics();
          p.beginFill(0x000000,0.4);
          p.lineStyle(1,0x000000,0.7);
          p.drawRoundedRect(0,0,w,h,r);
          p.endFill();
          let pp= p;//new PIXI.Sprite(p.generateCanvasTexture());
          pp.interactive = true;
          pp.buttonMode = true;
          pp.alpha = 1;
          pp.on('pointerover',function(){
              pp.alpha = 0.5;
          });
          pp.on('pointerout',function(){
              pp.alpha = 1;
          });
          pp.on('pointerdown',function(e){
              ret.command(e);
          });
          ctn.addChild(pp);
          return ret;
    };
     var _tpx_frame_ex = function(w,h,r){
          let ctn = new PIXI.Container();
          let ret ={};
          ret.width = w;
          ret.height = h;
          ret.command = function(){};
          ret.container = ctn;
          let p = new PIXI.Graphics();
          p.beginFill(0xffffff,1);
          p.lineStyle(4,0x000000,0.7);
          p.drawRoundedRect(0,0,w-4,h-4,r);
          p.endFill();
          let pp= p;//new PIXI.Sprite(p.generateCanvasTexture());
          pp.tint = Math.random()*0xffffff;
          pp.interactive = true;
          pp.buttonMode = true;
          pp.alpha = 1;
          pp.on('pointerover',function(){
              pp.alpha = 0.5;
          });
          pp.on('pointerout',function(){
              pp.alpha = 1;
          });
          pp.on('pointerdown',function(e){
              ret.command(e);
          });
          ctn.addChild(pp);
          return ret;
    };
    var _tpx_player = function(n){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 160;
        let h = 160;
        ret.width = w;
        ret.container = ctn;
        let pp = _tpx_frame_ex(w,h,4);
        ctn.addChild(pp.container);
        let tp = new PIXI.Text(n,{fontFamily: _oswaldFamily,fontWeight: '900',fontSize: 45,fill: "yellow"});
        tp.anchor.set(0.5);
        tp.x = w/2;
        tp.y = 0;
        pp.container.addChild(tp);
        let bb = new PIXI.Container();
        pp.container.addChild(bb);
        ret.number = tp;
        let wg = new PIXI.Text('0',{fontFamily: _oswaldFamily,fontWeight: '500',fontSize: 32,fill: "white",});
        wg.anchor.set(0.5);
        wg.x = (w)/2;
        wg.y = (h)/2;
        ctn.addChild(wg);
        ret.wagerBox = pp;
        ret.wager = wg;
        let px= _tpx_frame_ex(w,80,4);
        let ava = new PIXI.Sprite(PIXI.Texture.WHITE);
        ava.width = 80;
        ava.height = 80;
        //ava.tint = 0xA09F9B;
        ava.x = (w-80)/2;
        ava.y = 0;
        const mp = new PIXI.Graphics();
        mp.beginFill(0x000000,1);
        mp.lineStyle(2,0x000000,1);
        mp.drawRoundedRect(0,0,64,64,16);
        mp.endFill();
        mp.width = 66;
        mp.height = 66;
        mp.x = (w-66)/2;
        mp.y = (80-66)/2;
        px.container.addChild(mp);
        ava.mask = mp;
        px.container.addChild(ava);
        ret.avatar = ava;
        px.container.y = h+3;
        ctn.addChild(px.container);
        let pv= _tpx_frame(w+20,40,15);
        pv.container.x = -10;
        pv.container.y = 20;
        let st = new PIXI.Text('Sit Down',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:30,fill: "yellow",});
        st.anchor.set(0.5);
        st.x = 90;
        st.y = 20;
        ret.title = function(nm){
            st.text = nm;
            st.scale.set(1);
            if(st.width>=(w)*0.80){
                st.scale.set((1-(st.width-(w))/st.width)*0.8);
            }
            st.x = 90;
            st.y = 20;
        };
        pv.container.addChild(st);
        px.container.addChild(pv.container);
        ret.seatBox = pv;
        ret.height = pp.height+px.height+10;
        ret.handList=[];
        ret.betBox = bb;
        ret.position = function(s,x,y){ctn.scale.set(s);ctn.x = x;ctn.y = y;};
        return ret;
    };
    return{
        player : _tpx_player,
    };
}());