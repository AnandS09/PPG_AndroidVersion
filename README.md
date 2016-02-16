# PPG_AndroidVersion
Android application to demonstrate real time PPG using OpenCV

<h4>
This Android app will measure the heartbeat in beats per minute of the user using a smartphone camera. 
</h4>
<ul> 
  <li> Works with android v5.1.1
  <li> Requires OpenCV Manager preinstalled on device
</ul>

<b> Steps to install the app. </b> <br>
<font size="8"><i><b> NOTE:</b> This app is currently for for android lollipop v5.1.1 . I'll add the apks for other versions shortly </i></font>
<ol>
  <li> If you are a git user fork this repo. Otherwise you can download the project as a .zip file. </li>
  <li> Copy the .apk file present in the /apk/ directory into you phone's internal storage / SD card. </li>
  <li> In your smartphone click on the .apk file to install it. <i> Make sure you have marked "Install apps from SD card" option set, otherwise android will not allow you to install this app. </i> </li>
  <li> That's it! 
</ol>

<b> Using the app </b> <br>
<ol>
  <li><i> When you launch the app for the first time you might get a prompt asking you to install OpenCV manager if the same is not installed already on your phone. Please click yes and install the same. <a href="https://play.google.com/store/apps/details?id=org.opencv.engine&hl=en">OpenCV manager</a> is a required dependency for this app.</i></li>
  <li>Gently place your finger on your camera lens and start the app. Once the flashlight turns on you should see a complete red preview on the UI. <i>(If your finger is not correctly placed the app might produce errors)</i></li>
  <li> Wait till the text colour of the BPM value changes to green </li>
</ol>
<i> NOTE : The blue colour values are approximations and might be errorneus. The green colour indicates sufficient samples have been collected thus the vaule is reliable </i>

<b><i> DISCLAIMER : This is just an experimental app and the user should not intend to use it for medical purposes </i></b> 
