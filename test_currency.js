const val = "1000000";
console.log(new Intl.NumberFormat('en-US').format(parseInt(val.replace(/,/g, ''), 10)));
