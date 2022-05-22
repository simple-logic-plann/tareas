class Params {
  String viajeId;
  int tiempo;
  double multa;
  int minutosMulta;
  int minutosAnticipacion;
  Params(
      {required this.viajeId,
      required this.tiempo,
      required this.multa,
      required this.minutosAnticipacion,
      required this.minutosMulta});

  Map<String, dynamic> get toMap => {
        "viajeId": viajeId,
        "tiempo": tiempo,
        "multa": multa,
        "minutosMulta": minutosMulta,
        "minutosAnticipacion": minutosAnticipacion,
      };
}
