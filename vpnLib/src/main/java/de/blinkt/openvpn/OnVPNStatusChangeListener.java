package de.blinkt.openvpn;

public interface OnVPNStatusChangeListener {
    public void onProfileLoaded(boolean profileLoaded);
    public void onVPNStatusChanged(String status);

}
