<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page isELIgnored="false"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <script src="../resources/jquery/jquery-1.9.1.min.js"></script>
<link href="../resources/bootstrap/css/bootstrap.min.css" rel="stylesheet"> 
 <script src="../resources/bootstrap/js/bootstrap.min.js"></script>
 <script src="../resources/chart/Chart.bundle.js"></script> 
 
<title>math040</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style type="text/css">
    canvas{
        -moz-user-select: none;
        -webkit-user-select: none;
        -ms-user-select: none;
    }
     #chartjs-tooltip {
      opacity: 1;
      position: absolute;
      background: rgba(0, 0, 0, .7);
      color: white;
      border-radius: 3px;
      -webkit-transition: all .1s ease;
      transition: all .1s ease;
      pointer-events: none;
      -webkit-transform: translate(-50%, 0);
      transform: translate(-50%, 0);
    }
    .chartjs-tooltip-key {
      display: inline-block;
      width: 10px;
      height: 10px;
    }
    .padding-top{
         padding-top:40px
        }  
    </style>
      
</head>
<body>
<c:set var="menu" scope="request" value="statistics"/>  
 <div class="container-fluid">
	<div class="row">
		<div class="col col-md-12 col-sm-12">
		 <jsp:include page="menu.jsp" flush="true">
				 	<jsp:param name="menu" value="${menu}"/> 
		</jsp:include> 
			
			<div class="row">
			
		<div class="col-md-4 col-sm-4 pull-right  "> 
				<div class="padding-top"></div>
				<div class="btn-group-vertical" id="usernameButtons" data-toggle="buttons">
				  <label class="btn btn-default">
				     <input type="checkbox" name="usernames" id="option1" value="gang" autocomplete="off" >gang
				  </label>
				  <label class="btn btn-default">
				    <input type="checkbox" name="usernames" id="option2"  value="song" autocomplete="off">song
				  </label>
				  <label class="btn btn-default">
				    <input type="checkbox" name="usernames" id="option3"  value="liang" autocomplete="off">liang
				  </label>
				</div>
		</div>
		<div class="col-md-8 col-sm-8"> 
		    <div style="width:100%;">
		        <canvas id="canvas"></canvas>
		    </div> 
		</div>
	</div>
</div>
    <script>
         
        
        var randomColorFactor = function() {
            return Math.round(Math.random() * 255);
        };
        var randomColor = function(opacity) {
            return 'rgba(' + randomColorFactor() + ',' + randomColorFactor() + ',' + randomColorFactor() + ',' + (opacity || '.3') + ')';
        };
 
        var sequenceBetIds = [3,2,1,4,5,6];
        var allData=[];
        var gang = {
        		 name:'gang',
        		 datasets:[
        		 						{betId:1,title:'debt1',amount:29},
        		 						{betId:2,title:'debt2',amount:-23},
        		 						{betId:3,title:'debt3',amount:-23}, 
        		 						{betId:6,title:'debt6',amount:13} 
        		 					]	
        };
        var song = {
        	name:'song',
        		 datasets:[
        		 						{betId:1,title:'debt1',amount:23},
        		 						{betId:2,title:'debt2',amount:29}, 
        		 						{betId:5,title:'debt5',amount:13}, 
        		 					]	
        	 
        }; 
       
       allData.push(gang);
       allData.push(song); 
       
       
       var getBetIndex = function(betId){
       		var result=-1;
       	  $.each(sequenceBetIds,function(i,sequenceBetId){
       	  	if(sequenceBetId == betId){
       	  		result = i;
       	  		return;
       	  	}
       	  });
       	  return result;
       };
       
       //console.log(getBetIndex(10));
       
       var filterDataset = function(names){
   					var result = []; 
            if(!names || names.length<=0){
            	return result;
            }  
            $.each(names, function(i,name){
            	$.each(allData, function(i,dataset){
            		if(name == dataset.name){
            			var newObject = jQuery.extend(true, {}, dataset);
            			result.push(newObject);
            		}
            	});
            });
            return result;
       };
       
       
       
       var getDisplayLabel = function(filterDatasets){
       		var result = [];
       		if(filterDatasets.length<=0){
       			return result;
       		}
       		var betids = [];
       		$.each(filterDatasets,function(i,dataset){
       			$.each(dataset.datasets,function(i,bet){ 
       				if($.inArray(bet.betId,betids)<0){
       					betids.push(bet.betId);
       					result.push(bet);
       				}
       			}); 
       		});
       		return sortBetsLabel(result);
       }; 
        
       var sortBetsLabel = function(bets){
       	 if(!bets || bets.length<=0){
       	    return [];	
       	 }
       	 result = bets.sort(function(a,b){
       	 	 return getBetIndex(a.betId)-  getBetIndex(b.betId);
       	 });
       	 return result;
       };
       
       //console.log(getDisplayLabel(filterDataset(['song','gang'])));
       
       var getBetById = function(bets,betLabel){
       	 var result = null; 
       	 $.each(bets,function(i,bet){
       	 		if(bet.betId == betLabel.betId){
       	 			result = bet;
       	 			return;
       	 		} 
       	 });
       	 if(result==null){
       	 	 result = {
       	 	 	 betId:betLabel.betId,
       	 	 	 title:betLabel.title,
       	 	 	 amount:0
       	 	 }
       	 }
       	 return result;
       };
       
       var sortBetsDataSet = function(betsLabel,filterDatasets){ 
       		$.each(filterDatasets,function(i, dataset){
       			var sortedBets = []; 
       			var bets = dataset.datasets;
       			$.each(betsLabel,function(j,betLabel){
       				 sortedBets.push(getBetById(bets,betLabel)); 
       			});
       			dataset.datasets = sortedBets;
       		});
       		return filterDatasets;
       };
       
        var config = {
            type: 'line',
            data: {
                labels: [],
                datasets: []
            },
            options: {
                responsive: true,
                legend: { 
                    position: 'bottom',
                    onClick:null,
                },
                hover: {
                    mode: 'label'
                },
                scales: {
                    xAxes: [{
                        display: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Bet'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Amount'
                        }
                    }]
                },
                title: {
                    display: true,
                    text: 'Chart.js Line Chart - Legend'
                },
                animation: {
                        onComplete: function () {
                            var chartInstance = this.chart;
                            var ctx = chartInstance.ctx;
                            ctx.textAlign = "center";

                            Chart.helpers.each(this.data.datasets.forEach(function (dataset, i) {
                                var meta = chartInstance.controller.getDatasetMeta(i);
                                Chart.helpers.each(meta.data.forEach(function (line, index) {
                                	   //console.log(dataset.label);
                                    ctx.fillText(dataset.data[index],
                                     line._model.x, line._model.y  );
                                }),this)
                            }),this);
                        }
                      }
            }
        }; 
       
       var generateConfigs = function(selectedNames){
       	    config.data.labels = [];
       			config.data.datasets = [];
       			var datasets = filterDataset(selectedNames); 
       			//console.log(datasets);
            var betsLabel = getDisplayLabel(datasets);
            datasets = sortBetsDataSet(betsLabel,datasets); 
       			$.each(datasets,function(i,dataset){
		         	  var chart_dataset =  {
		         	  				label: dataset.name,
		                    data: [],
		                    fill: false,
		         	  }; 
			         $.each(dataset.datasets,function(j,bet){ 
			         		 config.data.labels.push(bet.title); 
				        	 chart_dataset.data.push(bet.amount);  
			         })
			         config.data.datasets.push(chart_dataset); 
        		}); 
        
		        $.each(config.data.datasets, function(i, dataset) {
		            var background = randomColor(0.5);
		            dataset.borderColor = background;
		            dataset.backgroundColor = background;
		            dataset.pointBorderColor = background;
		            dataset.pointBackgroundColor = background;
		            dataset.pointBorderWidth = 1;
		            dataset.pointRadius=8;
		        });
       };
                
         
				$(document).ready(function(){ 
					 
					
            var selectedNames = ['song','gang']; 
            generateConfigs(selectedNames); 
            
		        var ctx = document.getElementById("canvas").getContext("2d");
	          window.myLine = new Chart(ctx, config);
	          
	         $(".btn").change(function(){
	              var lb = $(this);
	              	lb.removeClass("btn-primary");
	              	lb.removeClass("btn-default");
	              if(lb.find("input[name='usernames']")[0].checked){
	            	  lb.addClass("btn-primary");
	              }else{
	            	  lb.addClass("btn-default");
	              } 
	              var usernames = $("#usernameButtons input:checked").map(function(){
	              	return $(this).val();
	              }).get() ; 
	             //console.log(usernames);
	             generateConfigs(usernames);
	             window.myLine.update();
	         });
        
		}); 

       
 
    </script>
</body>


</html>