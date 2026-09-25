import client from './client';
export interface FxRate {
  id: string;
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  source: string;
  fetchedAt: string;
}

export interface FxRatesResponse {
  base: string;
  supported: string[];
  rates: FxRate[];
}

export interface FxConvertResponse {
  from: string;
  to: string;
  amount: number;
  converted: number;
  rate: number;
}

export const getFxRates = async (): Promise<FxRatesResponse> => {
  const { data } = await client.get('/v1/fx/rates');
  return data;
};

export const convertCurrency = async (
  amount: number,
  from: string,
  to: string
): Promise<FxConvertResponse> => {
  const { data } = await client.get(
    `/v1/fx/convert?amount=${amount}&from=${from}&to=${to}`
  );
  return data;
};