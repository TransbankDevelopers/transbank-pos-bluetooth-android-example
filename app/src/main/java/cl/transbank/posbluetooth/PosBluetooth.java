package cl.transbank.posbluetooth;

import com.ingenico.pclutilities.PclUtilities;

import lombok.Getter;

@Getter
public class PosBluetooth {
    private String name;
    private String address;
    private boolean activated;

    public PosBluetooth(PclUtilities.BluetoothCompanion btCompanion) {
        this.name = btCompanion.getBluetoothDevice().getName();
        this.address = btCompanion.getBluetoothDevice().getAddress();
        this.activated = btCompanion.isActivated();
    }
}
