// Programa que combina tipos, condicionales y repeat-until
float precio = 100.0;
int descuento = 20;
bool aplicar = true;

if (aplicar) {
    precio = precio - descuento;
}

print("Precio final:");
print(precio);

// Mostrar tabla de precios con recargo
int i = 1;
repeat {
    float conRecargo = precio + (precio * i);
    print(conRecargo);
    i = i + 1;
} until (i > 3);