/*
 * The MIT License
 *
 * Copyright (c) 2016-17 Nishant Pathak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *
 */

package com.nishant.mocket_demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements
  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

  private static final int REQUEST_PERMISSION = 1;

  private GoogleApiClient mGoogleApiClient;

  private boolean isGpsDialogShown = false;

  private MocketClient mMocketClient;

  private LatLng latLng;
  private GoogleMap mGoogleMap;
  private Marker currLocationMarker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    SupportMapFragment mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mFragment.getMapAsync(this);

    // Initialize mocket client
    mMocketClient = MocketClient.getInstance();
    mMocketClient.init();
  }

  @Override
  protected void onDestroy() {
    mMocketClient.shutDown();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    if(!enabled) {
      showDialogGPS();
    }
  }

  /**
   * Show a dialog to the user requesting that GPS be enabled
   */
  private void showDialogGPS() {
    if (isGpsDialogShown) {
      return;
    }
    isGpsDialogShown = true;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false);
    builder.setTitle(R.string.enable_gps);
    builder.setMessage(R.string.please_enable_gps);
    builder.setPositiveButton(R.string.enable, (dialog, which) -> startActivity(
      new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
    builder.setNegativeButton(R.string.ignore, (dialog, which) -> dialog.dismiss());
    AlertDialog alert = builder.create();
    alert.show();
  }

  private void requestPermission() {
    ActivityCompat.requestPermissions(
      this,
      new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
      REQUEST_PERMISSION);
  }

  @Override
  public void onMapReady(GoogleMap gMap) {
    mGoogleMap = gMap;
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermission();
      return;
    }
    mGoogleMap.setMyLocationEnabled(true);
    buildGoogleApiClient();
    mGoogleApiClient.connect();

  }

  protected synchronized void buildGoogleApiClient() {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
  }

  @Override
  public void onConnected(Bundle bundle) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermission();
      return;
    }
    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    if (mLastLocation != null) {
      latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
      mMocketClient.pushLatLngToServer(latLng);

      MarkerOptions markerOptions = new MarkerOptions();
      markerOptions.position(latLng);
      markerOptions.title("Current Position");
      markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
      currLocationMarker = mGoogleMap.addMarker(markerOptions);
    }

    LocationRequest mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(5000); //5 seconds
    mLocationRequest.setFastestInterval(3000); //3 seconds
    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLocationChanged(Location location) {

    //place marker at current position
    //mGoogleMap.clear();
    if (currLocationMarker != null) {
      currLocationMarker.remove();
    }
    latLng = new LatLng(location.getLatitude(), location.getLongitude());
    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.position(latLng);
    markerOptions.title(getString(R.string.current_position));
    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
    currLocationMarker = mGoogleMap.addMarker(markerOptions);

    //zoom to current position:
    CameraPosition cameraPosition = new CameraPosition.Builder()
      .target(latLng).zoom(14).build();

    mGoogleMap.animateCamera(CameraUpdateFactory
      .newCameraPosition(cameraPosition));

    mMocketClient.pushLatLngToServer(latLng);
  }
}
