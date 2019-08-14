var CRAPS = (function(){
     var _oswaldFamily = 'Oswald, Arial';
     var _tpx_puck = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 80;
        ret.width = w;
        ret.height = w;
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0x000000,0.8);
        p.lineStyle(5,0x000000,0.3);
        p.drawCircle(0,0,w/2);
        p.endFill();
        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        ctn.addChild(pl);
        let t = new PIXI.Text('OFF',{fontWeight: '900',fontFamily:_oswaldFamily,fontSize:35,fill:'white'});
        t.anchor.set(0.5);
        //t.x = (w/2);
        //t.y = (w/2);
        ret.status = t;
        ctn.addChild(t);
        ret.start = 0;
        ret.duration = 1000;
        ret.x = 0;
        ret.xBase = 0;
        ret.update = function(nx){
            requestAnimationFrame(function(tm){
                ret.xBase = ret.x;
                ret.x = nx+50;
                ret.start = tm;
                requestAnimationFrame(ret.frame);
            });
        };
        ret.frame = function(tm){
            let ds = tm-ret.start;
            if(ds<ret.duration){
                let delta = (ret.x-ret.xBase)*(ds/ret.duration);
                ctn.x = (parseFloat(ret.xBase)+(parseFloat(delta)));
                requestAnimationFrame(ret.frame);
            }
            else{
                ctn.x = ret.x;
            }
        };
        return ret;
     };
     var _tpx_number = function(n,r,f,c){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        let w =100;
        ret.width = w;
        ret.height = w;
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0x00cc00,1);
        p.lineStyle(5,0xffffff,1);
        p.drawRect(0,0,w,w);
        p.endFill();
        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pl.hitArea = new PIXI.Rectangle(0,0,w,w);
        pl.interactive = true;
        pl.buttonMode = true;
        pl.alpha = 1;
        pl.on('pointerover',function(){
            pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);
        let t = new PIXI.Text(n,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:f,fill:c});
        t.anchor.set(0.5);
        t.x = (w/2);
        t.y = (w/2);
        t.rotation = r;
        ret.status = t;
        ctn.addChild(t);
        return ret;
    };
     var _tpx_passline = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        let w = 100;
        ret.width = 600;
        ret.height = w;
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0x00cc00,1);
        p.lineStyle(5,0xffffff,1);
        p.moveTo(0,100);
        p.lineTo(0,500);
        p.arcTo(0,600,100,600,100);
        p.lineTo(600,600);
        p.lineTo(600,500);
        p.lineTo(100,500);
        p.lineTo(100,0);
        p.lineTo(0,100);
        p.endFill();
        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pl.hitArea = new PIXI.Polygon(0,100,0,500,100,600,600,600,600,500,100,500,100,0);
        pl.interactive = true;
        pl.buttonMode = true;
        pl.alpha = 1;
        pl.on('pointerover',function(){
            pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);
        let t = new PIXI.Text('Pass Line',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:50,fill:'white'});
        t.anchor.set(0.5);
        t.x = (w/2);
        t.y = w*3;
        t.rotation = Math.PI/2;

        let t1 = new PIXI.Text('Pass Line',{fontWeight: '900',fontFamily:_oswaldFamily,fontSize:50,fill:'white'});
        t1.anchor.set(0.5);
        t1.x = 350;
        t1.y = 550;

        ctn.addChild(t);
        ctn.addChild(t1);

        return ret;
    };
    var _tpx_not_come_bar = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        let w = 160;
        let h = 180;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        let p = new PIXI.Graphics();

        p.beginFill(0x00cc00,1);
        p.lineStyle(5,0xffffff,1);
        p.drawRect(0,0,w,h);
        p.endFill();

        p.beginFill(0xfff933,1);
        p.lineStyle(0,0xfff933,1);
        p.drawRoundedRect(30,h-60,50,50,2);
        p.drawRoundedRect(w-70,h-60,50,50,2);
        p.endFill();

        p.beginFill(0x00FF00,1);
        p.lineStyle(0,0xffffff,1);
        p.drawCircle(40,h-50,6,6);
        p.drawCircle(40,h-35,6,6);
        p.drawCircle(40,h-20,6,6);
        p.drawCircle(70,h-50,6,6);
        p.drawCircle(70,h-35,6,6);
        p.drawCircle(70,h-20,6,6);
        p.drawCircle(w-60,h-50,6,6);
        p.drawCircle(w-60,h-35,6,6);
        p.drawCircle(w-60,h-20,6,6);
        p.drawCircle(w-30,h-50,6,6);
        p.drawCircle(w-30,h-35,6,6);
        p.drawCircle(w-30,h-20,6,6);
        p.endFill();

        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pl.hitArea = new PIXI.Rectangle(0,0,w,h);
        pl.interactive = true;
        pl.buttonMode = true;
        pl.alpha = 1;
        pl.on('pointerover',function(){
            pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);

        let t1 = new PIXI.Text('Don\'t\n Come\n Bar',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:28,fill:'white',align:'center'});
        t1.anchor.set(0.5);
        t1.x = w/2;
        t1.y = 70;
        ctn.addChild(t1);

        return ret;
    };
    var _tpx_not_pass_bar_2 = function(){
         let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         let w = 400;
         let h = 98
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();

         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
         p.drawRect(0,0,w,h);
         p.endFill();

         p.beginFill(0xfff933,1);
         p.lineStyle(0,0xfff933,1);
         p.drawRoundedRect(10,h-75,50,50,2);
         p.drawRoundedRect(70,h-75,50,50,2);
         p.endFill();

         p.beginFill(0x00FF00,1);
         p.lineStyle(0,0xffffff,1);

         p.drawCircle(20,h-65,6,6);
         p.drawCircle(20,h-50,6,6);
         p.drawCircle(20,h-35,6,6);

         p.drawCircle(50,h-65,6,6);
         p.drawCircle(50,h-50,6,6);
         p.drawCircle(50,h-35,6,6);

         p.drawCircle(80,h-65,6,6);
         p.drawCircle(80,h-50,6,6);
         p.drawCircle(80,h-35,6,6);
         p.drawCircle(110,h-65,6,6);
         p.drawCircle(110,h-50,6,6);
         p.drawCircle(110,h-35,6,6);
         p.endFill();

         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.hitArea = new PIXI.Rectangle(0,0,w,h);
         pl.interactive = true;
         pl.buttonMode = true;
         pl.alpha = 1;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ctn.addChild(pl);

         let t1 = new PIXI.Text('Don\'t Pass Bar',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:40,fill:'white'});

         t1.x = (w-t1.width)*0.8;
         t1.y = (h-t1.height)/2;

         ctn.addChild(t1);

         return ret;
     };

    var _tpx_not_pass_bar = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        let w = 100;
        let h = 300;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        let p = new PIXI.Graphics();

        p.beginFill(0x00cc00,1);
        p.lineStyle(5,0xffffff,1);
        p.drawRect(0,0,w,h);
        p.endFill();

        p.beginFill(0xfff933,1);
        p.lineStyle(0,0xfff933,1);
        p.drawRoundedRect(w/2-25,h-120,50,50,2);
        p.drawRoundedRect(w/2-25,h-60,50,50,2);
        p.endFill();

        p.beginFill(0x00FF00,1);
        p.lineStyle(0,0xffffff,1);
        p.drawCircle(w/2+15,h-110,6,6);
        p.drawCircle(w/2,h-110,6,6);
        p.drawCircle(w/2-15,h-110,6,6);
        p.drawCircle(w/2+15,h-80,6,6);
        p.drawCircle(w/2,h-80,6,6);
        p.drawCircle(w/2-15,h-80,6,6);

        p.drawCircle(w/2+15,h-50,6,6);
        p.drawCircle(w/2,h-50,6,6);
        p.drawCircle(w/2-15,h-50,6,6);
        p.drawCircle(w/2+15,h-20,6,6);
        p.drawCircle(w/2,h-20,6,6);
        p.drawCircle(w/2-15,h-20,6,6);
        p.endFill();

        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pl.hitArea = new PIXI.Rectangle(0,0,w,h);
        pl.interactive = true;
        pl.buttonMode = true;
        pl.alpha = 1;
        pl.on('pointerover',function(){
            pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);

        let t1 = new PIXI.Text('Don\'t Pass Bar',{fontWeight: '900',fontFamily:_oswaldFamily,fontSize:28,fill:'white'});
        t1.anchor.set(0.5);
        t1.x = w/2;
        t1.y = w;
        t1.rotation = Math.PI/2;
        ctn.addChild(t1);

        return ret;
    };
     var _tpx_big_8 = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        let w = 160;
        let h = 160;
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        let p = new PIXI.Graphics();

        p.beginFill(0x00cc00,1);
        p.lineStyle(5,0xffffff,1);
        p.moveTo(w-30,30);
        p.lineTo(w,60);
        p.lineTo(w,w);
        p.lineTo(0,w);
        p.lineTo(w-30,30);
        p.endFill();
        let pl =p;// new PIXI.Sprite(p.generateCanvasTexture());
        pl.hitArea = new PIXI.Polygon(w,0,w,w,0,w);
        pl.interactive = true;
        pl.buttonMode = true;
        pl.alpha = 1;
        pl.on('pointerover',function(){
            pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);

        let t1 = new PIXI.Text('8',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:70,fill:'red'});
        t1.anchor.set(0.5);
        t1.x = w-w/3;
        t1.y = w-w/3;
        t1.rotation = Math.PI/4;
        ctn.addChild(t1);

        return ret;
    };
    var _tpx_big_6 = function(){
         let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         let w = 160;
         let h = 160;
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();

         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
         p.moveTo(0,0);
         p.lineTo(100,0);
         p.lineTo(w-30,30);
         p.lineTo(0,w-1);
         p.lineTo(0,0);
         p.endFill();
         let pl =p;// new PIXI.Sprite(p.generateCanvasTexture());
         pl.hitArea = new PIXI.Polygon(0,0,0,w,w,0);
         pl.interactive = true;
         pl.buttonMode = true;
         pl.alpha = 1;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ctn.addChild(pl);

         let t1 = new PIXI.Text('6',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:70,fill:'red'});
         t1.anchor.set(0.5);
         t1.x = w/3;
         t1.y = h/3;
         t1.rotation = Math.PI/4;
         ctn.addChild(t1);

         return ret;
     };
      var _tpx_come = function(){
        let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         let w = 300;
         let h = 180;
            ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();

         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
         p.drawRect(0,0,w,h);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.hitArea = new PIXI.Rectangle(0,0,w,h);
         pl.interactive = true;
         pl.buttonMode = true;
         pl.alpha = 1;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ctn.addChild(pl);

         let t1 = new PIXI.Text('COME',{fontWeight: '900',fontFamily:_oswaldFamily,fontSize:90,fill:'red'});

         t1.x = (w-t1.width)/2;
         t1.y = (h-t1.height)/2;

         ctn.addChild(t1);

         return ret;
     };
     var _tpx_field = function(){
        let ctn = new PIXI.Container();
         let ret ={};
         let w = 460;
         let h = 180;
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         ret.command = function(){};
         let p = new PIXI.Graphics();

         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
         p.moveTo(0,0);
         p.lineTo(0,120);
         p.lineTo(60,h)
         p.lineTo(w,h);
         p.lineTo(w,0);
         p.lineTo(0,0);
         p.endFill();

         p.lineStyle(4,0xfff933,1);
         p.drawCircle(70,110,30);
         p.drawCircle(w-70,110,30);

         let t2 = new PIXI.Text('2',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:40,fill:'red'});
         t2.x = 60;
         t2.y = 90;

         let t12 = new PIXI.Text('12',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:40,fill:'red'});
         t12.x = w-85;
         t12.y = 90;


         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.hitArea = new PIXI.Rectangle(0,0,w,h);
         pl.interactive = true;
         pl.buttonMode = true;
         pl.alpha = 1;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ctn.addChild(pl);
         let t = new PIXI.Text('3     4     5     9     10',{fontWeight: '900',fontFamily:_oswaldFamily,fontSize:40,fill:'yellow'});
         t.x = (w-t.width)/2;
         t.y = (h-t.height)/4;
         ctn.addChild(t);
         let t1 = new PIXI.Text('FIELD',{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:60,fill:'yellow'});

         t1.x = (w-t1.width)/2;
         t1.y = (h-t1.height)-10;

         ctn.addChild(t1);
         ctn.addChild(t2);
          ctn.addChild(t12);
         return ret;
     };
     var _tpx_buy_lay = function(){
         let ret={};
         let ctn = new PIXI.Container();
         ret.container = ctn;
         let w = 100;
         let h = 60;
         let p = new PIXI.Graphics();
         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
         p.drawRect(0,0,w,h);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         ctn.addChild(pl);
         return ret;
     };
     var _tpx_side_hard_way = function(){
        let ret ={width:0,height:0};
        let pview = new PIXI.Container();
        ret.container = pview;
         let s0 = _tpx_side_line_b('Any Seven','8 FOR 1');
         pview.addChild(s0.container);
         ret.width=s0.width;
         ret.height +=s0.height;
         let s1 = _tpx_side_line([1,0,0,1,0,0,1],[1,0,0,1,0,0,1],'10 FOR 1');
         s1.container.y = s0.container.y+s0.height;
         pview.addChild(s1.container);
         let s2 = _tpx_side_line([1,0,1,1,1,0,1],[1,0,1,1,1,0,1],'8 FOR 1');
         s2.container.y = s1.container.y;
         s2.container.x = s1.container.x+s1.width;
         pview.addChild(s2.container);
         ret.height +=s2.height;
         let s3 = _tpx_side_line([1,0,1,0,1,0,1],[1,0,1,0,1,0,1],'10 FOR 1');
         s3.container.y = s1.container.y+s1.height;
         pview.addChild(s3.container);
         let s4 = _tpx_side_line([1,0,0,0,0,0,1],[1,0,0,0,0,0,1],'8 FOR 1');
         s4.container.y = s3.container.y;
         s4.container.x = s3.container.x + s3.width;
         pview.addChild(s4.container);
         ret.height +=s4.height;
         ret.s0 = s0;
         ret.s1 = s1;
         ret.s2 = s2;
         ret.s3 = s3;
         ret.s4 = s4;
        return ret;
     };
     var _tpx_side_one_roll = function(){
        let ret ={width:0,height:0};
        let pview = new PIXI.Container();
        ret.container = pview;
         let s5 = _tpx_side_line_s([0,0,0,1,0,0,0],[1,0,0,0,0,0,1],'8 FOR 1');
         pview.addChild(s5.container);
         ret.height +=s5.height;
         let s6 = _tpx_side_line_s([0,0,0,1,0,0,0],[0,0,0,1,0,0,0],'8 FOR 1');
         s6.container.y = s5.container.y;
         s6.container.x = s5.container.x+s5.width;
         pview.addChild(s6.container);
         let s7 = _tpx_side_line_s([1,1,1,0,1,1,1],[1,1,1,0,1,1,1],'8 FOR 1');
         s7.container.y = s6.container.y;
         s7.container.x = s6.container.x+s6.width;
         pview.addChild(s7.container);
         ret.height +=s7.height;
         let s8 = _tpx_side_line([1,1,1,0,1,1,1],[1,0,1,1,1,0,1],'8 FOR 1');
         s8.container.y = s7.container.y+s7.height;
         pview.addChild(s8.container);
         let s9 = _tpx_side_line([1,0,1,1,1,0,1],[1,1,1,0,1,1,1],'8 FOR 1');
         s9.container.y = s8.container.y;
         s9.container.x = s8.container.x+s8.width;
         pview.addChild(s9.container);
         ret.height +=s9.height;
         let s10 = _tpx_side_line_b('Any Craps','8 FOR 1');
         s10.container.y = s9.container.y+s9.height;
         pview.addChild(s10.container);
         ret.width = s10.width;
         ret.s5 = s5;
         ret.s6 = s6;
         ret.s7 = s7;
         ret.s8 = s8;
         ret.s9 = s9;
         ret.s10 = s10;

         return ret;
     };
      var _tpx_side_line = function(d1,d2,t){
        let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         let w = 165;
         let h = 120;
            ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();

         p.beginFill(0x00cc00,1);
         p.lineStyle(5,0xffffff,1);
            p.drawRect(0,0,w,h);
         p.endFill();

         p.beginFill(0xfff933,1);
         p.lineStyle(0,0xfff933,1);
         p.drawRect(25,10,50,50);
         p.drawRect(w-70,10,50,50);
         p.endFill();

         p.beginFill(0xff1a1a,1);
         p.lineStyle(0,0xff1a1a,1);
         if(d1[0]===1){p.drawCircle(35,20,6,6);}
         if(d1[1]===1){p.drawCircle(35,35,6,6);}
         if(d1[2]===1){p.drawCircle(35,50,6,6);}
         if(d1[3]===1){p.drawCircle(50,35,6,6);}//middle
         if(d1[4]===1){p.drawCircle(65,20,6,6);}
         if(d1[5]===1){p.drawCircle(65,35,6,6);}
         if(d1[6]===1){p.drawCircle(65,50,6,6);}

         if(d2[0]===1){p.drawCircle(w-60,20,6,6);}
         if(d2[1]===1){p.drawCircle(w-60,35,6,6);}
         if(d2[2]===1){p.drawCircle(w-60,50,6,6);}
         if(d2[3]===1){p.drawCircle(w-45,35,6,6);}//middle
         if(d2[4]===1){p.drawCircle(w-30,20,6,6);}
         if(d2[5]===1){p.drawCircle(w-30,35,6,6);}
         if(d2[6]===1){p.drawCircle(w-30,50,6,6);}
         p.endFill();

         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.hitArea = new PIXI.Rectangle(0,0,w,h);
         pl.interactive = true;
         pl.buttonMode = true;
         pl.alpha = 1;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ctn.addChild(pl);

         let t1 = new PIXI.Text(t,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:40,fill:'white',align:'center'});
         t1.anchor.set(0.5);
         t1.x = w/2;
         t1.y = 100;
         ctn.addChild(t1);

         return ret;
      };
       var _tpx_side_line_s = function(d1,d2,t){
        let ctn = new PIXI.Container();
          let ret ={};
          ret.command = function(){};
          let w = 110;
          let h = 120;
            ret.width = w;
          ret.height = h;
          ret.container = ctn;
          let p = new PIXI.Graphics();

          p.beginFill(0x00cc00,1);
          p.lineStyle(5,0xffffff,1);
            p.drawRect(0,0,w,h);
          p.endFill();

          p.beginFill(0xfff933,1);
          p.lineStyle(0,0xfff933,1);
          p.drawRect(10,20,40,40);
          p.drawRect(w-50,20,40,40);
          p.endFill();

          p.beginFill(0xff1a1a,1);
          p.lineStyle(0,0xff1a1a,1);
            if(d1[0]===1){p.drawCircle(18,28,4,4);}
          if(d1[1]===1){p.drawCircle(18,40,4,4);}
          if(d1[2]===1){p.drawCircle(18,52,4,4);}
          if(d1[3]===1){p.drawCircle(30,40,4,4);}//middle
          if(d1[4]===1){p.drawCircle(42,28,4,4);}
          if(d1[5]===1){p.drawCircle(42,40,4,4);}
          if(d1[6]===1){p.drawCircle(42,52,4,4);}

          if(d2[0]===1){p.drawCircle(w-42,28,4,4);}
          if(d2[1]===1){p.drawCircle(w-42,40,4,4);}
          if(d2[2]===1){p.drawCircle(w-42,52,4,4);}
          if(d2[3]===1){p.drawCircle(w-30,40,4,4);}//middle
          if(d2[4]===1){p.drawCircle(w-18,28,4,4);}
          if(d2[5]===1){p.drawCircle(w-18,40,4,4);}
          if(d2[6]===1){p.drawCircle(w-18,52,4,4);}
          p.endFill();

          let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
          pl.hitArea = new PIXI.Rectangle(0,0,w,h);
          pl.interactive = true;
          pl.buttonMode = true;
          pl.alpha = 1;
          pl.on('pointerover',function(){
              pl.alpha = 0.5;
          });
          pl.on('pointerout',function(){
              pl.alpha = 1;
          });
          pl.on('pointerdown',function(e){
              ret.command(e);
          });
          ctn.addChild(pl);

          let t1 = new PIXI.Text(t,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:26,fill:'white',align:'center'});
          t1.anchor.set(0.5);
          t1.x = w/2;
          t1.y = 90;
          ctn.addChild(t1);

          return ret;
      };
      var _tpx_side_line_b = function(t,pt){
        let ctn = new PIXI.Container();
          let ret ={};
          ret.command = function(){};
          let w = 330;
          let h = 120;
            ret.width = w;
          ret.height = h;
          ret.container = ctn;
          let p = new PIXI.Graphics();
          p.beginFill(0x00cc00,1);
          p.lineStyle(5,0xffffff,1);
            p.drawRect(0,0,w,h);
          p.endFill();
          let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
          pl.interactive = true;
          pl.buttonMode = true;
          pl.alpha = 1;
          pl.on('pointerover',function(){
              pl.alpha = 0.5;
          });
          pl.on('pointerout',function(){
              pl.alpha = 1;
          });
          pl.on('pointerdown',function(e){
              ret.command(e);
          });
          ctn.addChild(pl);

          let t1 = new PIXI.Text(t,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:40,fill:'red'});
          t1.anchor.set(0.5);
          t1.x = w/2;
          t1.y = 40;
          ctn.addChild(t1);
          let t2 = new PIXI.Text(pt+"         "+pt,{fontWeight: '900',fontFamily: _oswaldFamily,fontSize:30,fill:'white'});
          t2.anchor.set(0.5);
          t2.x = w/2;
          t2.y = 90;
          ctn.addChild(t2);

          return ret;
      };
       var _tpx_dice_set = function(){
          let ctn = new PIXI.Container();
          let ret ={};
          let dice1 = new PIXI.Sprite(PIXI.Texture.WHITE);
          dice1.anchor.set(0.5);
          dice1.width = 96;
          dice1.height = 96;
          ctn.addChild(dice1);
          let dice2 = new PIXI.Sprite(PIXI.Texture.WHITE);
          dice2.anchor.set(0.5);
          dice2.width = 96;
          dice2.height = 96;
          dice2.x = 100;
          dice2.y = 4;
          ctn.addChild(dice2);
          ret.dice1 = dice1;
          ret.dice2 = dice2;
          ret.width = 200;
          ret.height = 100;
          ret.container = ctn;
          ret.start = 0;
          ret.duration = 1000;
          ret.x = 0;
          ret.y = 0;
          ret.xBase = 0;
          ret.yBase = 0;
          ret.list =[];
          ret.d1 = 3;
          ret.d2 = 4;
          ret.seq=0;
          ret.callback = function(){};
          ret.set = function(d1,d2,cb){
            ret.d1 = d1;
            ret.d2 = d2;
            ret.callback = cb;
          };
          ret.update = function(x,y,nx,ny){
            requestAnimationFrame(function(tm){
                ret.xBase = x;
                  ret.x = nx;
                  ret.yBase = y;
                  ret.y = ny;
                  ret.start = tm;
                  ret.seq = 0;
                  requestAnimationFrame(ret.frame);
              });
          };
          ret.frame = function(tm){
            let ds = tm-ret.start;
              if(ds<ret.duration){
                let deltax = (ret.x-ret.xBase)*(ds/ret.duration);
                  ctn.x = (parseFloat(ret.xBase)+(parseFloat(deltax)));
                  let deltay = (ret.y-ret.yBase)*(ds/ret.duration);
                  ctn.y = (parseFloat(ret.yBase)+(parseFloat(deltay)));
                  if(ret.seq>ret.list.length-2){
                    ret.seq=0;
                  }
                  dice1.texture = ret.list[ret.seq];
                  dice1.rotation += 0.1*deltax;//Math.PI/ret.seq;
                  dice2.texture = ret.list[ret.seq+1];
                  dice2.rotation = (-0.1)*deltay;
                  ret.seq++;
                  requestAnimationFrame(ret.frame);
              }
              else{
                ctn.x = ret.x;
                ctn.y = ret.y;
                dice1.texture = ret.list[ret.d1];
                dice2.texture = ret.list[ret.d2];
                ret.callback();
              }
          };
          return ret;
       };
       var _tpx_side = function(){
          let ret ={};
          let pview = new PIXI.Container();
          ret.container = pview;
          let top_line_set = _tpx_side_hard_way();
          let low_line_set = _tpx_side_one_roll();
          let s0 = top_line_set.s0;
          let s1 = top_line_set.s1;
          let s2 = top_line_set.s2;
          let s3 = top_line_set.s3;
          let s4 = top_line_set.s4;
          let s5 = low_line_set.s5;
          let s6 = low_line_set.s6;
          let s7 = low_line_set.s7;
          let s8 = low_line_set.s8;
          let s9 = low_line_set.s9;
          let s10 = low_line_set.s10;

          s0.lineId = 401;
          s0.betList = new PIXI.Container();
          s0.container.addChild(s0.betList);
          ret.anySeven = s0;

          s1.lineId = 333;
          s1.betList = new PIXI.Container();
          s1.container.addChild(s1.betList);
          ret.hard6 = s1;

          s2.lineId = 355;
          s2.betList = new PIXI.Container();
          s2.container.addChild(s2.betList);
          ret.hard10 = s2;

          s3.lineId = 344;
          s3.betList = new PIXI.Container();
          s3.container.addChild(s3.betList);
          ret.hard8 = s3;

          s4.lineId = 322;
          s4.betList = new PIXI.Container();
          s4.container.addChild(s4.betList);
          ret.hard4 = s4;

          s5.lineId = 404;
          s5.betList = new PIXI.Container();
          s5.container.addChild(s5.betList);
          ret.on3 = s5;

          s6.lineId = 403;
          s6.betList = new PIXI.Container();
          s6.container.addChild(s6.betList);
          ret.on2 = s6;

          s7.lineId = 407;
          s7.betList = new PIXI.Container();
          s7.container.addChild(s7.betList);
          ret.on12 = s7;

          s8.lineId = 405;
          s8.betList = new PIXI.Container();
          s8.container.addChild(s8.betList);
          ret.on11 = s8;

          s9.lineId = 406;
          s9.betList = new PIXI.Container();
          s9.container.addChild(s9.betList);
          ret.on112 = s9;

          s10.lineId = 402;
          s10.betList = new PIXI.Container();
          s10.container.addChild(s10.betList);
          ret.anyCraps = s10;

          ret.topset = top_line_set;
          ret.lowset = low_line_set;
          return ret;
      };
      var _tpx_table = function(){
        let ret ={};
        let pview = new PIXI.Container();
        ret.container = pview;
        let puck = _tpx_puck();
        puck.status.text = 'OFF';
        puck.container.x  = 0;
        puck.container.y = 40;
         ret.puck = puck;
         ret.lines = [];
         let p7 = _tpx_passline();
         p7.container.x  = 0;
         p7.container.y = 120;
         p7.lineId = 100;
         p7.betList = new PIXI.Container();
         p7.container.addChild(p7.betList);
         ret.lines[0]=(p7);
         pview.addChild(p7.container);
         ret.passLine = p7;
         let c7 = _tpx_not_pass_bar();
         ret.doNotPassLine = c7;
         c7.container.x  = p7.height;
         c7.container.y = 160;
         c7.lineId = 200;
         c7.betList = new PIXI.Container();
         c7.container.addChild(c7.betList);
         ret.lines[1]=(c7);
         pview.addChild(c7.container);
         let d7 = _tpx_not_come_bar();
         ret.doNotCome = d7;
         d7.container.x  = 200;
         d7.container.y = 160;
         d7.lineId = 202;
         d7.betList = new PIXI.Container();
         d7.container.addChild(d7.betList);
         ret.lines[2]=(d7);
         pview.addChild(d7.container);
         let n4 = _tpx_number(4,0,70,'yellow');
         ret.place4 = n4;
         n4.container.x  = c7.container.x;
         n4.container.y = 0;
         n4.lineId = 104;
         n4.betList = new PIXI.Container();
         n4.container.addChild(n4.betList);
         ret.lines[3]=(n4);
         pview.addChild(n4.container);
         let by4 = _tpx_buy_lay();
         by4.container.x = n4.container.x;
         by4.container.y = n4.container.y+n4.height;
         pview.addChild(by4.container);
         let n5 = _tpx_number(5,0,70,'yellow');
         ret.place5 = n5;
         n5.container.x  = n4.container.x+100;
         n5.container.y = 0;
         n5.lineId = 105;
         n5.betList = new PIXI.Container();
         n5.container.addChild(n5.betList);
         ret.lines[4]=(n5);
         pview.addChild(n5.container);
         let by5 = _tpx_buy_lay();
         by5.container.x = n5.container.x;
         by5.container.y = n4.container.y+n4.height;
         pview.addChild(by5.container);
         let n6 = _tpx_number('SIX',-45,35,'red');
         ret.place6 = n6;
         n6.container.x  = n5.container.x+100;
         n6.container.y = 0;
         n6.lineId = 106;
         n6.betList = new PIXI.Container();
         n6.container.addChild(n6.betList);
         ret.lines[5]=(n6);
         pview.addChild(n6.container);
         let by6 = _tpx_buy_lay();
         by6.container.x = n6.container.x;
         by6.container.y = n4.container.y+n4.height;
         pview.addChild(by6.container);
         let n8 = _tpx_number(8,0,70,'yellow');
         ret.place8 = n8;
         n8.container.x  = n6.container.x+100;
         n8.container.y = 0;
         n8.lineId = 108;
         n8.betList = new PIXI.Container();
         n8.container.addChild(n8.betList);
         ret.lines[6]=(n8);
         pview.addChild(n8.container);
         let by8 = _tpx_buy_lay();
         by8.container.x = n8.container.x;
         by8.container.y = n4.container.y+n4.height;
         pview.addChild(by8.container);
         let n9 = _tpx_number('NINE',-45,30,'red');
         ret.place9 = n9;
         n9.container.x  = n8.container.x+100;
         n9.container.y = 0;
         n9.lineId = 109;
         n9.betList = new PIXI.Container();
         n9.container.addChild(n9.betList);
         ret.lines[7]=(n9);
         pview.addChild(n9.container);
         let by9 = _tpx_buy_lay();
         by9.container.x = n9.container.x;
         by9.container.y = n4.container.y+n4.height;
         pview.addChild(by9.container);
         let n10 = _tpx_number(10,0,70,'yellow');
         ret.place10 = n10;
         n10.container.x  = n9.container.x+100;
         n10.container.y = 0;
         n10.lineId = 110;
         n10.betList = new PIXI.Container();
         n10.container.addChild(n10.betList);
         ret.lines[8]=(n10);
         pview.addChild(n10.container);
         let by10 = _tpx_buy_lay();
         by10.container.x = n10.container.x;
         by10.container.y = n4.container.y+n4.height;
         pview.addChild(by10.container);
         let cm = _tpx_come();
         ret.come = cm;
         cm.container.x  = 360;
         cm.container.y = 160;
         cm.lineId = 101;
         cm.betList = new PIXI.Container();
         cm.container.addChild(cm.betList);
         ret.lines[9]=(cm);
         pview.addChild(cm.container);
         let fd = _tpx_field();
         ret.field = fd;
         fd.container.x  = 200;
         fd.container.y = 340;
         fd.lineId = 400;
         fd.betList = new PIXI.Container();
         fd.container.addChild(fd.betList);
         ret.lines[10]=(fd);
         pview.addChild(fd.container);
         let b6 = _tpx_big_6();
         ret.big6 = b6;
         b6.container.x  = 100;
         b6.container.y = 460;
         b6.lineId = 306;
         b6.betList = new PIXI.Container();
         b6.container.addChild(b6.betList);
         ret.lines[11]=(b6);
         pview.addChild(b6.container);
         let b8 = _tpx_big_8();
         ret.big8 = b8;
         b8.container.x  =100;
         b8.container.y = 460;
         b8.lineId = 308;
         b8.betList = new PIXI.Container();
         b8.container.addChild(b8.betList);
         ret.lines[12]=(b8);
         pview.addChild(b8.container);
         let v7 = _tpx_not_pass_bar_2();
         ret.doNotPassLine2 = v7;
         v7.container.x  = 260;
         v7.container.y = 520;
         v7.lineId = 201;
         v7.betList = new PIXI.Container();
         v7.container.addChild(v7.betList);
         ret.lines[13]=(v7);
         pview.addChild(v7.container);
         let side = _tpx_side();
         ret.lines[14]=side.anySeven;
         ret.lines[15]=side.hard4;
         ret.lines[16]=side.hard6;
         ret.lines[17]=side.hard8;
         ret.lines[18]=side.hard10;
         ret.lines[19]=side.on2;
         ret.lines[20]=side.on3;
         ret.lines[21]=side.on12;
         ret.lines[22]=side.on11;
         ret.lines[23]=side.on112;
         ret.lines[24]=side.anyCraps;
         ret.side = side;
         pview.addChild(puck.container);
         let diceSet = _tpx_dice_set();
         diceSet.container.x = 10;
         diceSet.container.y = 620;
         pview.addChild(diceSet.container);
         ret.diceSet = diceSet;
         ret.width = 700;
         ret.height = 725;
         return ret;
     };
    return{
        table : _tpx_table,
    };
})();