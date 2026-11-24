package com.devst.inventariovicecity.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.inventariovicecity.R;
import com.devst.inventariovicecity.model.Articulo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Pinta cada artículo como una tarjeta del inventario
class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.Fila> {

    private final List<Articulo> datos = new ArrayList<>();

    void establecerDatos(List<Articulo> nuevos) {
        datos.clear();
        if (nuevos != null) {
            datos.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.item_articulo, padre, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        fila.enlazar(datos.get(posicion));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {

        private final TextView tvInicial;
        private final TextView tvNombre;
        private final TextView tvDetalle;
        private final TextView tvPrecio;
        private final TextView tvCantidad;
        private final TextView tvEstado;

        Fila(@NonNull View vista) {
            super(vista);
            tvInicial = vista.findViewById(R.id.tvInicial);
            tvNombre = vista.findViewById(R.id.tvNombre);
            tvDetalle = vista.findViewById(R.id.tvDetalle);
            tvPrecio = vista.findViewById(R.id.tvPrecio);
            tvCantidad = vista.findViewById(R.id.tvCantidad);
            tvEstado = vista.findViewById(R.id.tvEstado);
        }

        void enlazar(Articulo articulo) {
            String nombre = articulo.getNombre();
            String inicial = nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase(Locale.getDefault());
            tvInicial.setText(inicial);
            tvNombre.setText(nombre);
            tvDetalle.setText(articulo.getCodigo() + " · " + articulo.getCategoria());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%,d", articulo.getPrecio()));
            tvCantidad.setText(String.valueOf(articulo.getCantidad()));

            // El estado sale de la cantidad: 0 agotado, 1 o 2 pocas, 3 o más en stock
            int cantidad = articulo.getCantidad();
            if (cantidad <= 0) {
                tvEstado.setText("AGOTADO");
                tvEstado.setBackgroundResource(R.drawable.bg_estado_agotado);
            } else if (cantidad <= 2) {
                tvEstado.setText("POCAS");
                tvEstado.setBackgroundResource(R.drawable.bg_estado_pocas);
            } else {
                tvEstado.setText("EN STOCK");
                tvEstado.setBackgroundResource(R.drawable.bg_estado_en_stock);
            }
        }
    }
}
